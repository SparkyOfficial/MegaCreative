package com.megacreative.coding.functions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.DefaultScriptEngine;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.actions.control.ReturnAction;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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
 * Test class for function return functionality
 */
public class FunctionReturnTest {

    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Player player;
    
    @Mock
    private VariableManager variableManager;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    private AdvancedFunctionManager functionManager;
    private ReturnAction returnAction;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up plugin mock
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("FunctionReturnTest"));
        
        // Create function manager
        functionManager = new AdvancedFunctionManager(plugin);
        
        // Create return action
        returnAction = new ReturnAction(plugin);
    }
    
    @Test
    void testReturnActionExecution() {
        // Create a code block with return value
        CodeBlock block = mock(CodeBlock.class);
        when(block.getParameterValue("return_value")).thenReturn("test_value");
        
        // Create execution context
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .build();
        
        // Execute return action
        ExecutionResult result = returnAction.execute(block, context);
        
        // Verify results
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.isTerminated());
        assertEquals("test_value", result.getReturnValue());
        assertEquals("Function returned", result.getMessage());
        
        // Verify that the return value was set in context
        assertEquals("test_value", context.getVariable("return"));
    }
    
    @Test
    void testReturnActionWithoutValue() {
        // Create a code block without return value
        CodeBlock block = mock(CodeBlock.class);
        when(block.getParameterValue("return_value")).thenReturn(null);
        
        // Create execution context
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .build();
        
        // Execute return action
        ExecutionResult result = returnAction.execute(block, context);
        
        // Verify results
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.isTerminated());
        assertNull(result.getReturnValue());
        assertEquals("Function returned", result.getMessage());
    }
    
    @Test
    void testFunctionWithReturn() {
        // Create a simple function with a return statement
        List<FunctionDefinition.FunctionParameter> params = new ArrayList<>();
        
        List<CodeBlock> functionBlocks = new ArrayList<>();
        
        // Create a return block
        CodeBlock returnBlock = mock(CodeBlock.class);
        when(returnBlock.getAction()).thenReturn("return");
        when(returnBlock.getParameterValue("return_value")).thenReturn("Hello World");
        functionBlocks.add(returnBlock);
        
        // Create function definition
        FunctionDefinition function = new FunctionDefinition(
            "testFunction",
            "A test function with return",
            player,
            params,
            functionBlocks,
            ValueType.TEXT,
            FunctionDefinition.FunctionScope.PLAYER
        );
        
        // Register function
        boolean registered = functionManager.registerFunction(function);
        assertTrue(registered, "Function should be registered successfully");
        
        // Execute function
        DataValue[] arguments = new DataValue[0];
        CompletableFuture<ExecutionResult> future = functionManager.executeFunction("testFunction", player, arguments);
        
        // Get result
        ExecutionResult result = future.join();
        
        // Verify results
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Hello World", result.getReturnValue());
    }
}