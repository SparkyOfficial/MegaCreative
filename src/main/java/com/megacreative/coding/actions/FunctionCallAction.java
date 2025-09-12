package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.coding.functions.FunctionDefinition;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 🎆 FrameLand-Style Function Call Action
 * 
 * Handles execution of user-defined functions within the coding system.
 * Supports function discovery, parameter passing, and result handling.
 */
public class FunctionCallAction implements BlockAction {
    
    private final MegaCreative plugin;
    private final AdvancedFunctionManager functionManager;
    
    public FunctionCallAction(MegaCreative plugin) {
        this.plugin = plugin;
        this.functionManager = plugin.getServiceRegistry().getService(AdvancedFunctionManager.class);
    }

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (block == null || player == null) {
            return ExecutionResult.error("Invalid execution context for function call");
        }
        
        // Get function name from block parameters
        String functionName = block.getParameterValue("function_name", String.class);
        if (functionName == null || functionName.trim().isEmpty()) {
            return ExecutionResult.error("Function name is required");
        }
        
        // Find function definition
        FunctionDefinition function = functionManager.findFunction(functionName, player);
        if (function == null) {
            return ExecutionResult.error("Function not found: " + functionName);
        }
        
        // Parse function arguments from block parameters
        DataValue[] arguments = parseArguments(block, function);
        
        // Create context data for function execution
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("caller_block", block);
        contextData.put("execution_context", context);
        
        try {
            // Execute function synchronously for simplicity
            CompletableFuture<ExecutionResult> future = functionManager.executeFunction(functionName, player, arguments, contextData);
            ExecutionResult result = future.get(); // This blocks, but for action compatibility
            
            // Handle function result
            if (result.isSuccess()) {
                // Store return value in context if available
                Object returnValue = result.getReturnValue();
                if (returnValue != null) {
                    context.setVariable("last_function_result", returnValue);
                }
                
                plugin.getLogger().info("🎆 Function '" + functionName + "' executed successfully");
                return ExecutionResult.success("Function call completed: " + functionName);
            } else {
                plugin.getLogger().warning("🎆 Function '" + functionName + "' execution failed: " + result.getMessage());
                return result;
            }
        } catch (Exception e) {
            return ExecutionResult.error("Function execution error: " + e.getMessage());
        }
    }
    
    /**
     * Parses function arguments from block parameters
     */
    private DataValue[] parseArguments(CodeBlock block, FunctionDefinition function) {
        List<FunctionDefinition.FunctionParameter> expectedParams = function.getParameters();
        DataValue[] arguments = new DataValue[expectedParams.size()];
        
        for (int i = 0; i < expectedParams.size(); i++) {
            FunctionDefinition.FunctionParameter param = expectedParams.get(i);
            String paramKey = "arg_" + i + "_" + param.getName();
            
            // Try to get parameter value from block
            Object value = block.getParameterValue(paramKey);
            
            // If not found, try with just parameter name
            if (value == null) {
                value = block.getParameterValue(param.getName());
            }
            
            // If still not found, use default value
            if (value == null) {
                value = param.getDefaultValue();
            }
            
            // Convert to DataValue
            if (value != null) {
                arguments[i] = DataValue.fromObject(value);
            } else {
                // Create empty value of expected type
                arguments[i] = createEmptyValue(param.getType());
            }
        }
        
        return arguments;
    }
    
    /**
     * Creates an empty DataValue of the specified type
     */
    private DataValue createEmptyValue(ValueType type) {
        switch (type) {
            case TEXT:
                return DataValue.of("");
            case NUMBER:
                return DataValue.of(0.0);
            case BOOLEAN:
                return DataValue.of(false);
            case LOCATION:
                return DataValue.of(null);
            case ITEM:
                return DataValue.of(null);
            case PLAYER:
                return DataValue.of(null);
            default:
                return DataValue.of("");
        }
    }

}