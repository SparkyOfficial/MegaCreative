package com.megacreative.services;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeCompilerTest {

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

    private CodeCompiler codeCompiler;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        lenient().when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        lenient().when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        lenient().when(plugin.getBlockPlacementHandler()).thenReturn(blockPlacementHandler);
        lenient().when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        lenient().when(world.getName()).thenReturn("test_world_dev");

        codeCompiler = new CodeCompiler(plugin);
    }

    @Test
    void testConstructor() {
        assertNotNull(codeCompiler);
        assertNotNull(codeCompiler.getBlockConfigService());
    }

    @Test
    void testCompileWorldScripts_EmptyWorld() {
        // Arrange
        when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(Map.of());

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

        when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(codeBlocks);

        // Act
        List<CodeScript> result = codeCompiler.compileWorldScripts(world);

        // Assert
        assertNotNull(result);
        // Should have one script for the event block
        assertEquals(1, result.size());
        
        CodeScript script = result.get(0);
        assertNotNull(script);
        assertNotNull(script.getRootBlock());
        assertEquals("onJoin", script.getRootBlock().getAction());
        assertTrue(script.isEnabled());
        assertEquals(CodeScript.ScriptType.EVENT, script.getType());
    }

    @Test
    void testCompileWorldScripts_NonEventBlock() {
        // Arrange
        Location location = new Location(world, 0, 64, 0);
        CodeBlock actionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        actionBlock.setId(UUID.randomUUID());

        Map<Location, CodeBlock> codeBlocks = new HashMap<>();
        codeBlocks.put(location, actionBlock);

        when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(codeBlocks);

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
        // Should have one script only for the event block
        assertEquals(1, result.size());
        
        CodeScript script = result.get(0);
        assertNotNull(script);
        assertEquals("onJoin", script.getRootBlock().getAction());
    }

    @Test
    void testGetFunctionFromBlock_NullBlock() {
        // Use reflection to access the private method
        try {
            java.lang.reflect.Method method = CodeCompiler.class.getDeclaredMethod("getFunctionFromBlock", CodeBlock.class);
            method.setAccessible(true);
            
            // Act
            Object result = method.invoke(codeCompiler, (CodeBlock) null);
            
            // Assert
            assertNull(result);
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
    }

    @Test
    void testGetFunctionFromBlock_EventBlock() {
        // Use reflection to access the private method
        try {
            java.lang.reflect.Method method = CodeCompiler.class.getDeclaredMethod("getFunctionFromBlock", CodeBlock.class);
            method.setAccessible(true);
            
            // Arrange
            CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
            
            // Act
            Object result = method.invoke(codeCompiler, eventBlock);
            
            // Assert
            assertEquals("joinEvent", result);
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
    }

    @Test
    void testGetFunctionFromBlock_ActionBlock() {
        // Use reflection to access the private method
        try {
            java.lang.reflect.Method method = CodeCompiler.class.getDeclaredMethod("getFunctionFromBlock", CodeBlock.class);
            method.setAccessible(true);
            
            // Arrange
            CodeBlock actionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
            
            // Act
            Object result = method.invoke(codeCompiler, actionBlock);
            
            // Assert
            assertEquals("sendMessage", result);
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
    }

    @Test
    void testGetFunctionFromBlock_BracketBlock() {
        // Use reflection to access the private method
        try {
            java.lang.reflect.Method method = CodeCompiler.class.getDeclaredMethod("getFunctionFromBlock", CodeBlock.class);
            method.setAccessible(true);
            
            // Arrange
            CodeBlock bracketBlock = new CodeBlock(Material.PISTON, "BRACKET");
            bracketBlock.setBracketType(CodeBlock.BracketType.OPEN);
            
            // Act
            Object result = method.invoke(codeCompiler, bracketBlock);
            
            // Assert
            assertEquals("{", result);
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
    }

    @Test
    void testGetFunctionFromBlock_EmptyAction() {
        // Use reflection to access the private method
        try {
            java.lang.reflect.Method method = CodeCompiler.class.getDeclaredMethod("getFunctionFromBlock", CodeBlock.class);
            method.setAccessible(true);
            
            // Arrange
            CodeBlock block = new CodeBlock(Material.COBBLESTONE, "NOT_SET");
            
            // Act
            Object result = method.invoke(codeCompiler, block);
            
            // Assert
            assertNull(result);
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
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
        // Use reflection to access the private method
        try {
            java.lang.reflect.Method method = CodeCompiler.class.getDeclaredMethod("isEventBlock", CodeBlock.class);
            method.setAccessible(true);
            
            // Test with event block
            CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
            Object result1 = method.invoke(codeCompiler, eventBlock);
            assertTrue((Boolean) result1);
            
            // Test with non-event block
            CodeBlock actionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
            Object result2 = method.invoke(codeCompiler, actionBlock);
            assertFalse((Boolean) result2);
            
            // Test with null
            Object result3 = method.invoke(codeCompiler, (CodeBlock) null);
            assertFalse((Boolean) result3);
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
    }

    @Test
    void testFormatLocation() {
        // Use reflection to access the private method
        try {
            java.lang.reflect.Method method = CodeCompiler.class.getDeclaredMethod("formatLocation", Location.class);
            method.setAccessible(true);
            
            // Test with valid location
            Location location = new Location(world, 10, 64, 20);
            Object result = method.invoke(codeCompiler, location);
            assertEquals("(10, 64, 20)", result);
            
            // Test with null location
            Object result2 = method.invoke(codeCompiler, (Location) null);
            assertEquals("null", result2);
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
    }
}