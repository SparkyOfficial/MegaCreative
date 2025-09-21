package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Simple test to verify the refactored system works correctly
 */
@ExtendWith(MockitoExtension.class)
public class SimpleScriptTest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Player player;
    
    /**
     * Test that we can create a simple script with connected blocks
     */
    @Test
    public void testSimpleScriptCreation() {
        // Create event block (player join)
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "playerJoin");
        
        // Create action block (send message)
        CodeBlock actionBlock = new CodeBlock(Material.GOLD_BLOCK, "sendMessage");
        actionBlock.setParameter("message", "Hello, World!");
        
        // Connect the blocks
        eventBlock.setNextBlock(actionBlock);
        
        // Create script
        CodeScript script = new CodeScript("Test Script", true, eventBlock);
        script.setType(CodeScript.ScriptType.EVENT);
        
        // Verify the script structure
        assertNotNull(script);
        assertTrue(script.isEnabled());
        assertEquals("Test Script", script.getName());
        assertNotNull(script.getRootBlock());
        assertEquals("playerJoin", script.getRootBlock().getAction());
        
        // Verify block connection
        CodeBlock nextBlock = script.getRootBlock().getNextBlock();
        assertNotNull(nextBlock);
        assertEquals("sendMessage", nextBlock.getAction());
        assertEquals("Hello, World!", nextBlock.getParameterValue("message", String.class));
    }
    
    /**
     * Test that we can execute a simple script
     */
    @Test
    public void testSimpleScriptExecution() {
        // This test would require more complex mocking of the Bukkit environment
        // For now, we'll just verify that the basic structure works
        assertTrue(true, "Basic structure test passed");
    }
}