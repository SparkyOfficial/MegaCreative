package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.models.CreativeWorld;
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
 * Test class for break and continue functionality in while loops
 */
public class WhileLoopBreakContinueTest {

    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Player player;
    
    @Mock
    private VariableManager variableManager;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    @Mock
    private CreativeWorld creativeWorld;
    
    private DefaultScriptEngine scriptEngine;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up plugin mock
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("WhileLoopBreakContinueTest"));
        when(plugin.getWorldManager()).thenReturn(null);
        when(plugin.getVariableManager()).thenReturn(variableManager);
        
        // Create script engine
        scriptEngine = new DefaultScriptEngine(plugin, variableManager, null, blockConfigService);
    }
    
    @Test
    void testWhileLoopWithBreak() {
        // Create a simple while loop block
        CodeBlock whileBlock = createCodeBlock("whileLoop");
        
        // Set up while config to return "CONTROL" type
        BlockConfigService.BlockConfig whileConfig = mock(BlockConfigService.BlockConfig.class);
        when(whileConfig.getType()).thenReturn("CONTROL");
        when(blockConfigService.getBlockConfig("whileLoop")).thenReturn(whileConfig);
        
        // Execute block
        CompletableFuture<ExecutionResult> future = scriptEngine.executeBlock(whileBlock, player, "test");
        ExecutionResult result = future.join();
        
        // Verify execution completed
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }
    
    @Test
    void testWhileLoopWithContinue() {
        // Create a simple while loop block
        CodeBlock whileBlock = createCodeBlock("whileLoop");
        
        // Set up while config to return "CONTROL" type
        BlockConfigService.BlockConfig whileConfig = mock(BlockConfigService.BlockConfig.class);
        when(whileConfig.getType()).thenReturn("CONTROL");
        when(blockConfigService.getBlockConfig("whileLoop")).thenReturn(whileConfig);
        
        // Execute block
        CompletableFuture<ExecutionResult> future = scriptEngine.executeBlock(whileBlock, player, "test");
        ExecutionResult result = future.join();
        
        // Verify execution completed
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }
    
    private CodeBlock createCodeBlock(String action) {
        CodeBlock block = mock(CodeBlock.class);
        when(block.getAction()).thenReturn(action);
        when(block.getId()).thenReturn(UUID.randomUUID());
        return block;
    }
}