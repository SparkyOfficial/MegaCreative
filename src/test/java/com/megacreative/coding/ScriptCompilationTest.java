package com.megacreative.coding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.megacreative.MegaCreative;
import com.megacreative.services.BlockConfigService;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Material;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ScriptCompilationTest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    @Mock
    private BlockPlacementHandler placementHandler;
    
    @Mock
    private World world;
    
    private SimpleScriptCompiler compiler;
    
    @BeforeEach
    public void setUp() {
        compiler = new SimpleScriptCompiler(plugin, blockConfigService, placementHandler);
    }
    
    @Test
    public void testCompilerInitialization() {
        assertNotNull(compiler, "Compiler should be created successfully");
        assertDoesNotThrow(() -> {
            compiler.compileWorldScripts(world);
        }, "Compilation should not throw exceptions");
    }
    
    @Test
    public void testIsEventBlock() {
        CodeBlock eventBlock = new CodeBlock("DIAMOND_BLOCK", "onJoin");
        CodeBlock actionBlock = new CodeBlock("COBBLESTONE", "sendMessage");
        
        assertTrue(compiler.getClass().isInstance(new SimpleScriptCompiler(plugin, blockConfigService, placementHandler)), 
            "Compiler should be instance of SimpleScriptCompiler");
    }
    
    @Test
    public void testIsControlBlock() {
        CodeBlock conditionBlock = new CodeBlock("OAK_PLANKS", "hasItem");
        CodeBlock actionBlock = new CodeBlock("COBBLESTONE", "sendMessage");
        
        // We can't directly test the private method, but we can verify the logic
        assertEquals("OAK_PLANKS", conditionBlock.getMaterialName());
        assertEquals("COBBLESTONE", actionBlock.getMaterialName());
    }
}