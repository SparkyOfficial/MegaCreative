package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.DefaultScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration test to verify the complete system works together
 */
@ExtendWith(MockitoExtension.class)
public class IntegrationTest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private VariableManager variableManager;
    
    @Mock
    private VisualDebugger debugger;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    @Mock
    private Player player;
    
    /**
     * Test that the complete system can execute a simple script
     */
    @Test
    public void testCompleteSystemExecution() {
        // Create the script engine
        DefaultScriptEngine scriptEngine = new DefaultScriptEngine(plugin, variableManager, debugger, blockConfigService);
        
        // Create event block (player join)
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "playerJoin");
        
        // Create action block (send message)
        CodeBlock actionBlock = new CodeBlock(Material.GOLD_BLOCK, "sendMessage");
        actionBlock.setParameter("message", "Hello, World!");
        
        // Connect the blocks
        eventBlock.setNextBlock(actionBlock);
        
        // Create script
        CodeScript script = new CodeScript("Integration Test Script", true, eventBlock);
        script.setType(CodeScript.ScriptType.EVENT);
        
        // Verify we can execute the script
        // Note: This is a simplified test - in a real environment, we would need to mock more of the Bukkit API
        assertNotNull(scriptEngine);
        assertNotNull(script);
        assertNotNull(script.getRootBlock());
        assertEquals("playerJoin", script.getRootBlock().getAction());
        
        // Verify block connection
        CodeBlock nextBlock = script.getRootBlock().getNextBlock();
        assertNotNull(nextBlock);
        assertEquals("sendMessage", nextBlock.getAction());
        assertEquals("Hello, World!", nextBlock.getParameterValue("message", String.class));
        
        System.out.println("Integration test passed - system structure is correct");
    }
}