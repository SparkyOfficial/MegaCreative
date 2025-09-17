package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.actions.control.ReturnAction;
import com.megacreative.coding.conditions.GenericBlockCondition;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for early return functionality in conditional blocks
 */
public class EarlyReturnInConditionalTest {

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
    private ReturnAction returnAction;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up plugin mock
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("EarlyReturnInConditionalTest"));
        when(plugin.getWorldManager()).thenReturn(null);
        when(plugin.getVariableManager()).thenReturn(variableManager);
        
        // Create script engine
        scriptEngine = new DefaultScriptEngine(plugin, variableManager, null, blockConfigService);
        
        // Create return action
        returnAction = new ReturnAction(plugin);
    }
    
    @Test
    void testEarlyReturnInIfBlock() {
        // Create a simple script with an if block that returns early
        CodeBlock rootBlock = createCodeBlock("test_action");
        
        // Create condition block
        CodeBlock conditionBlock = createCodeBlock("test_condition");
        conditionBlock.setNextBlock(rootBlock);
        
        // Create if block
        CodeBlock ifBlock = createCodeBlock("conditionalBranch");
        ifBlock.setNextBlock(conditionBlock);
        
        // Create return block as child of if block
        CodeBlock returnBlock = createCodeBlock("return");
        when(returnBlock.getParameterValue("return_value")).thenReturn("Early return value");
        
        List<CodeBlock> ifChildren = new ArrayList<>();
        ifChildren.add(returnBlock);
        ifBlock.setChildren(ifChildren);
        
        // Set up condition config to return "CONTROL" type
        BlockConfigService.BlockConfig ifConfig = mock(BlockConfigService.BlockConfig.class);
        when(ifConfig.getType()).thenReturn("CONTROL");
        when(blockConfigService.getBlockConfig("conditionalBranch")).thenReturn(ifConfig);
        
        // Set up return config to return "ACTION" type
        BlockConfigService.BlockConfig returnConfig = mock(BlockConfigService.BlockConfig.class);
        when(returnConfig.getType()).thenReturn("ACTION");
        when(blockConfigService.getBlockConfig("return")).thenReturn(returnConfig);
        
        // Set up condition config to return "CONDITION" type
        BlockConfigService.BlockConfig conditionConfig = mock(BlockConfigService.BlockConfig.class);
        when(conditionConfig.getType()).thenReturn("CONDITION");
        when(blockConfigService.getBlockConfig("test_condition")).thenReturn(conditionConfig);
        
        // Set up root config to return "ACTION" type
        BlockConfigService.BlockConfig rootConfig = mock(BlockConfigService.BlockConfig.class);
        when(rootConfig.getType()).thenReturn("ACTION");
        when(blockConfigService.getBlockConfig("test_action")).thenReturn(rootConfig);
        
        // Execute block
        CompletableFuture<ExecutionResult> future = scriptEngine.executeBlock(ifBlock, player, "test");
        ExecutionResult result = future.join();
        
        // Verify that execution was terminated
        assertNotNull(result);
        assertTrue(result.isTerminated());
        assertEquals("Early return value", result.getReturnValue());
    }
    
    @Test
    void testEarlyReturnInElseBlock() {
        // Create a simple script with an else block that returns early
        CodeBlock rootBlock = createCodeBlock("test_action");
        
        // Create condition block
        CodeBlock conditionBlock = createCodeBlock("test_condition");
        conditionBlock.setNextBlock(rootBlock);
        
        // Create else block
        CodeBlock elseBlock = createCodeBlock("else");
        elseBlock.setNextBlock(conditionBlock);
        
        // Create return block as child of else block
        CodeBlock returnBlock = createCodeBlock("return");
        when(returnBlock.getParameterValue("return_value")).thenReturn("Else return value");
        
        List<CodeBlock> elseChildren = new ArrayList<>();
        elseChildren.add(returnBlock);
        elseBlock.setChildren(elseChildren);
        
        // Set up else config to return "CONTROL" type
        BlockConfigService.BlockConfig elseConfig = mock(BlockConfigService.BlockConfig.class);
        when(elseConfig.getType()).thenReturn("CONTROL");
        when(blockConfigService.getBlockConfig("else")).thenReturn(elseConfig);
        
        // Set up return config to return "ACTION" type
        BlockConfigService.BlockConfig returnConfig = mock(BlockConfigService.BlockConfig.class);
        when(returnConfig.getType()).thenReturn("ACTION");
        when(blockConfigService.getBlockConfig("return")).thenReturn(returnConfig);
        
        // Set up condition config to return "CONDITION" type
        BlockConfigService.BlockConfig conditionConfig = mock(BlockConfigService.BlockConfig.class);
        when(conditionConfig.getType()).thenReturn("CONDITION");
        when(blockConfigService.getBlockConfig("test_condition")).thenReturn(conditionConfig);
        
        // Set up root config to return "ACTION" type
        BlockConfigService.BlockConfig rootConfig = mock(BlockConfigService.BlockConfig.class);
        when(rootConfig.getType()).thenReturn("ACTION");
        when(blockConfigService.getBlockConfig("test_action")).thenReturn(rootConfig);
        
        // Set up execution context
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .creativeWorld(creativeWorld)
            .currentBlock(elseBlock)
            .build();
        context.setLastConditionResult(false);
        
        // Execute block
        CompletableFuture<ExecutionResult> future = scriptEngine.executeBlock(elseBlock, player, "test");
        ExecutionResult result = future.join();
        
        // Verify that execution was terminated
        assertNotNull(result);
        assertTrue(result.isTerminated());
        assertEquals("Else return value", result.getReturnValue());
    }
    
    private CodeBlock createCodeBlock(String action) {
        CodeBlock block = mock(CodeBlock.class);
        when(block.getAction()).thenReturn(action);
        when(block.getId()).thenReturn(UUID.randomUUID());
        return block;
    }
}