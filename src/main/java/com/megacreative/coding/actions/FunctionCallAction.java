package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
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
public class FunctionCallAction extends CodeAction {
    
    private final AdvancedFunctionManager functionManager;
    
    public FunctionCallAction(MegaCreative plugin) {
        super(plugin);
        this.functionManager = plugin.getServiceRegistry().getService(AdvancedFunctionManager.class);
    }

    @Override
    public CompletableFuture<ExecutionResult> execute(ExecutionContext context) {
        CodeBlock block = context.getCurrentBlock();
        Player player = context.getPlayer();
        
        if (block == null || player == null) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Invalid execution context for function call"));
        }
        
        // Get function name from block parameters
        String functionName = block.getParameterValue("function_name", String.class);
        if (functionName == null || functionName.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Function name is required"));
        }
        
        // Find function definition
        FunctionDefinition function = functionManager.findFunction(functionName, player);
        if (function == null) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Function not found: " + functionName));
        }
        
        // Parse function arguments from block parameters
        DataValue[] arguments = parseArguments(block, function);
        
        // Create context data for function execution
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("caller_block", block);
        contextData.put("execution_context", context);
        
        // Execute function
        return functionManager.executeFunction(functionName, player, arguments, contextData)
            .thenApply(result -> {
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
            });
    }
    
    /**
     * Parses function arguments from block parameters
     */
    private DataValue[] parseArguments(CodeBlock block, FunctionDefinition function) {
        List<FunctionDefinition.Parameter> expectedParams = function.getParameters();
        DataValue[] arguments = new DataValue[expectedParams.size()];
        
        for (int i = 0; i < expectedParams.size(); i++) {
            FunctionDefinition.Parameter param = expectedParams.get(i);
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
            case STRING:
                return new DataValue(ValueType.STRING, "");
            case NUMBER:
                return new DataValue(ValueType.NUMBER, 0.0);
            case BOOLEAN:
                return new DataValue(ValueType.BOOLEAN, false);
            case LOCATION:
                return new DataValue(ValueType.LOCATION, null);
            case ITEM:
                return new DataValue(ValueType.ITEM, null);
            case PLAYER:
                return new DataValue(ValueType.PLAYER, null);
            default:
                return new DataValue(ValueType.STRING, "");
        }
    }

    @Override
    public boolean canExecute(ExecutionContext context) {
        return context.getCurrentBlock() != null && 
               context.getPlayer() != null &&
               functionManager != null;
    }

    @Override
    public String getActionName() {
        return "call_function";
    }

    @Override
    public String getDescription() {
        return "🎆 Calls a user-defined function with specified arguments";
    }
}