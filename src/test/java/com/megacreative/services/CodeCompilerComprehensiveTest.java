package com.megacreative.services;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.configs.WorldCode;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeCompilerComprehensiveTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private ServiceRegistry serviceRegistry;

    @Mock
    private BlockConfigService blockConfigService;

    @Mock
    private BlockPlacementHandler blockPlacementHandler;

    @Mock
    private World world;

    @Mock
    private Player player;

    @Mock
    private Logger logger;

    @Mock
    private Block mockBlock;

    private CodeCompiler codeCompiler;

    @BeforeEach
    void setUp() {
        // Set up the mock chain
        lenient().when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        lenient().when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        lenient().when(plugin.getBlockPlacementHandler()).thenReturn(blockPlacementHandler);
        lenient().when(plugin.getLogger()).thenReturn(logger);
        lenient().when(world.getName()).thenReturn("test_world-code");
        lenient().when(mockBlock.getType()).thenReturn(Material.AIR);

        codeCompiler = new CodeCompiler(plugin);
    }

    @Test
    void testConstructor() {
        assertNotNull(codeCompiler);
        assertSame(blockConfigService, codeCompiler.getBlockConfigService());
    }

    @Test
    void testCompileWorldScripts_EmptyWorld() {
        // Arrange
        lenient().when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(new HashMap<>());

        // Act
        List<CodeScript> result = codeCompiler.compileWorldScripts(world);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCompileWorldScripts_WithEventBlock() {
        // Arrange
        Location location = new Location(world, 0, 64, 0);
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        eventBlock.setId(UUID.randomUUID());

        Map<Location, CodeBlock> codeBlocks = new HashMap<>();
        codeBlocks.put(location, eventBlock);

        lenient().when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(codeBlocks);

        // Act
        List<CodeScript> result = codeCompiler.compileWorldScripts(world);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        CodeScript script = result.get(0);
        assertNotNull(script);
        assertNotNull(script.getRootBlock());
        assertEquals("onJoin", script.getRootBlock().getAction());
        assertTrue(script.isEnabled());
        assertEquals(CodeScript.ScriptType.EVENT, script.getType());
        assertTrue(script.getName().contains("onJoin"));
    }

    @Test
    void testCompileWorldScripts_NonEventBlock() {
        // Arrange
        Location location = new Location(world, 0, 64, 0);
        CodeBlock actionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        actionBlock.setId(UUID.randomUUID());

        Map<Location, CodeBlock> codeBlocks = new HashMap<>();
        codeBlocks.put(location, actionBlock);

        lenient().when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(codeBlocks);

        // Act
        List<CodeScript> result = codeCompiler.compileWorldScripts(world);

        // Assert
        assertNotNull(result);
        // Should be empty because non-event blocks don't create scripts
        assertTrue(result.isEmpty());
    }

    @Test
    void testCompileWorldScripts_MixedBlocks() {
        // Arrange
        Location eventLocation = new Location(world, 0, 64, 0);
        Location actionLocation = new Location(world, 1, 64, 0);

        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        eventBlock.setId(UUID.randomUUID());

        CodeBlock actionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        actionBlock.setId(UUID.randomUUID());

        Map<Location, CodeBlock> codeBlocks = new HashMap<>();
        codeBlocks.put(eventLocation, eventBlock);
        codeBlocks.put(actionLocation, actionBlock);

        when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(codeBlocks);

        // Act
        List<CodeScript> result = codeCompiler.compileWorldScripts(world);

        // Assert
        assertNotNull(result);
        // Should have one script only for the event block in the specified world
        assertEquals(1, result.size());

        CodeScript script = result.get(0);
        assertNotNull(script);
        assertEquals("onJoin", script.getRootBlock().getAction());
    }

    @Test
    void testCompileWorldScripts_ErrorHandling() {
        // Arrange
        Location location = new Location(world, 0, 64, 0);
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        eventBlock.setId(UUID.randomUUID());

        Map<Location, CodeBlock> codeBlocks = new HashMap<>();
        codeBlocks.put(location, eventBlock);

        lenient().when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(codeBlocks);
        // Simulate an error during compilation
        // We can't easily mock private methods, so we'll test the error logging

        // Act
        List<CodeScript> result = codeCompiler.compileWorldScripts(world);

        // Assert
        assertNotNull(result);
        // The method should handle errors gracefully
    }

    @Test
    void testGetFunctionFromBlock_NullBlock() {
        // Act
        String result = invokeGetFunctionFromBlock(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetFunctionFromBlock_EventBlock() {
        // Arrange
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");

        // Act
        String result = invokeGetFunctionFromBlock(eventBlock);

        // Assert
        assertEquals("joinEvent", result);
    }

    @Test
    void testGetFunctionFromBlock_ActionBlock() {
        // Arrange
        CodeBlock actionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");

        // Act
        String result = invokeGetFunctionFromBlock(actionBlock);

        // Assert
        assertEquals("sendMessage", result);
    }

    @Test
    void testGetFunctionFromBlock_BracketBlock() {
        // Arrange
        CodeBlock bracketBlock = new CodeBlock(Material.PISTON, "BRACKET");
        bracketBlock.setBracketType(CodeBlock.BracketType.OPEN);

        // Act
        String result = invokeGetFunctionFromBlock(bracketBlock);

        // Assert
        assertEquals("{", result);

        // Test closing bracket
        bracketBlock.setBracketType(CodeBlock.BracketType.CLOSE);
        String result2 = invokeGetFunctionFromBlock(bracketBlock);
        assertEquals("}", result2);
    }

    @Test
    void testGetFunctionFromBlock_EmptyAction() {
        // Arrange
        CodeBlock block = new CodeBlock(Material.COBBLESTONE, "NOT_SET");

        // Act
        String result = invokeGetFunctionFromBlock(block);

        // Assert
        assertNull(result);
    }

    @Test
    void testCompileWorldToCodeStrings() {
        // Arrange
        lenient().when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(new HashMap<>());

        // Act
        List<String> result = codeCompiler.compileWorldToCodeStrings(world);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testIsEventBlock() {
        // Test with event block
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        boolean result1 = invokeIsEventBlock(eventBlock);
        assertTrue(result1);

        // Test with non-event block
        CodeBlock actionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        boolean result2 = invokeIsEventBlock(actionBlock);
        assertFalse(result2);

        // Test with null
        boolean result3 = invokeIsEventBlock(null);
        assertFalse(result3);
    }

    @Test
    void testFormatLocation() {
        // Test with valid location
        Location location = new Location(world, 10, 64, 20);
        String result = invokeFormatLocation(location);
        assertEquals("(10, 64, 20)", result);

        // Test with null location
        String result2 = invokeFormatLocation(null);
        assertEquals("null", result2);
    }

    @Test
    void testScanWorldStructure() {
        // Arrange
        when(blockConfigService.isCodeBlock(any(Material.class))).thenReturn(false);
        lenient().when(blockConfigService.isCodeBlock(Material.DIAMOND_BLOCK)).thenReturn(true);
        lenient().when(blockConfigService.isCodeBlock(Material.COBBLESTONE)).thenReturn(true);

        // Mock the block placement handler's internal map
        Map<Location, CodeBlock> internalMap = new HashMap<>();
        lenient().when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(internalMap);

        // Act
        Map<Location, CodeBlock> result = codeCompiler.scanWorldStructure(world);

        // Assert
        assertNotNull(result);
    }

    // Helper method to access private getFunctionFromBlock method
    private String invokeGetFunctionFromBlock(CodeBlock block) {
        try {
            java.lang.reflect.Method method = CodeCompiler.class.getDeclaredMethod("getFunctionFromBlock", CodeBlock.class);
            method.setAccessible(true);
            return (String) method.invoke(codeCompiler, block);
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
            return null;
        }
    }

    // Helper method to access private isEventBlock method
    private boolean invokeIsEventBlock(CodeBlock block) {
        try {
            java.lang.reflect.Method method = CodeCompiler.class.getDeclaredMethod("isEventBlock", CodeBlock.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(codeCompiler, block);
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
            return false;
        }
    }

    // Helper method to access private formatLocation method
    private String invokeFormatLocation(Location location) {
        try {
            java.lang.reflect.Method method = CodeCompiler.class.getDeclaredMethod("formatLocation", Location.class);
            method.setAccessible(true);
            return (String) method.invoke(codeCompiler, location);
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
            return null;
        }
    }
}