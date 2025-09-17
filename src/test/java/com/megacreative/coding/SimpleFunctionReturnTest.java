package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Simple integration test for function return functionality
 */
public class SimpleFunctionReturnTest {

    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Player player;
    
    @Mock
    private VariableManager variableManager;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    private DefaultScriptEngine scriptEngine;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up plugin mock
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("SimpleFunctionReturnTest"));
        when(plugin.getWorldManager()).thenReturn(null);
        when(plugin.getVariableManager()).thenReturn(variableManager);
        
        // Create script engine
        scriptEngine = new DefaultScriptEngine(plugin, variableManager, null, blockConfigService);
    }
    
    @Test
    void testScriptEngineInitialization() {
        // Test that the script engine can be created
        assertNotNull(scriptEngine);
    }
    
    @Test
    void testBasicExecution() {
        // Create a simple code block
        CodeBlock block = mock(CodeBlock.class);
        when(block.getAction()).thenReturn("test_action");
        when(block.getId()).thenReturn(UUID.randomUUID());
        
        // Set up config to return "ACTION" type
        BlockConfigService.BlockConfig config = mock(BlockConfigService.BlockConfig.class);
        when(config.getType()).thenReturn("ACTION");
        when(blockConfigService.getBlockConfig("test_action")).thenReturn(config);
        
        // Execute block
        CompletableFuture<ExecutionResult> future = scriptEngine.executeBlock(block, player, "test");
        ExecutionResult result = future.join();
        
        // Verify execution completed
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }
}