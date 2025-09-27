package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Actions for managing variables
 * Includes setting, getting, and manipulating variables in different scopes
 */
@BlockMeta(id = "variableActions", displayName = "§aVariable Actions", type = BlockType.ACTION)
public class VariableActions implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
        
        if (player == null || variableManager == null) {
            return ExecutionResult.error("No player or variable manager available");
        }
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        try {
            // Get operation type
            DataValue operationValue = block.getParameter("operation");
            if (operationValue == null) {
                return ExecutionResult.error("Operation parameter is required");
            }
            
            String operation = resolver.resolve(context, operationValue).asString();
            
            switch (operation.toLowerCase()) {
                case "set":
                    return setVariable(block, context, resolver, variableManager, player);
                    
                case "get":
                    return getVariable(block, context, resolver, variableManager, player);
                    
                case "increment":
                    return incrementVariable(block, context, resolver, variableManager, player);
                    
                case "decrement":
                    return decrementVariable(block, context, resolver, variableManager, player);
                    
                case "delete":
                    return deleteVariable(block, context, resolver, variableManager, player);
                    
                case "copy":
                    return copyVariable(block, context, resolver, variableManager, player);
                    
                case "exists":
                    return variableExists(block, context, resolver, variableManager, player);
                    
                default:
                    return ExecutionResult.error("Unknown variable operation: " + operation);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error in variable operation: " + e.getMessage());
        }
    }
    
    /**
     * Sets a variable value
     */
    private ExecutionResult setVariable(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                      VariableManager variableManager, Player player) {
        try {
            DataValue variableNameValue = block.getParameter("variableName");
            DataValue valueValue = block.getParameter("value");
            DataValue scopeValue = block.getParameter("scope");
            
            if (variableNameValue == null || valueValue == null) {
                return ExecutionResult.error("Variable name and value parameters are required");
            }
            
            String variableName = resolver.resolve(context, variableNameValue).asString();
            DataValue value = resolver.resolve(context, valueValue);
            String scopeStr = scopeValue != null ? resolver.resolve(context, scopeValue).asString() : "PLAYER";
            
            VariableScope scope = VariableScope.valueOf(scopeStr.toUpperCase());
            
            variableManager.setVariable(variableName, value, scope, player.getUniqueId().toString());
            
            return ExecutionResult.success("Variable '" + variableName + "' set successfully in " + scope + " scope");
        } catch (Exception e) {
            return ExecutionResult.error("Error setting variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets a variable value
     */
    private ExecutionResult getVariable(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                      VariableManager variableManager, Player player) {
        try {
            DataValue variableNameValue = block.getParameter("variableName");
            DataValue resultVar = block.getParameter("resultVariable");
            DataValue scopeValue = block.getParameter("scope");
            
            if (variableNameValue == null || resultVar == null) {
                return ExecutionResult.error("Variable name and result variable parameters are required");
            }
            
            String variableName = resolver.resolve(context, variableNameValue).asString();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            String scopeStr = scopeValue != null ? resolver.resolve(context, scopeValue).asString() : "PLAYER";
            
            VariableScope scope = VariableScope.valueOf(scopeStr.toUpperCase());
            DataValue value = variableManager.getVariable(variableName, scope, player.getUniqueId().toString());
            
            if (value != null) {
                context.setVariable(resultVariable, value);
                return ExecutionResult.success("Variable '" + variableName + "' retrieved successfully");
            } else {
                return ExecutionResult.error("Variable '" + variableName + "' not found in " + scope + " scope");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error getting variable: " + e.getMessage());
        }
    }
    
    /**
     * Increments a numeric variable
     */
    private ExecutionResult incrementVariable(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                            VariableManager variableManager, Player player) {
        try {
            DataValue variableNameValue = block.getParameter("variableName");
            DataValue incrementValue = block.getParameter("increment");
            DataValue scopeValue = block.getParameter("scope");
            
            if (variableNameValue == null) {
                return ExecutionResult.error("Variable name parameter is required");
            }
            
            String variableName = resolver.resolve(context, variableNameValue).asString();
            double increment = incrementValue != null ? resolver.resolve(context, incrementValue).asNumber().doubleValue() : 1.0;
            String scopeStr = scopeValue != null ? resolver.resolve(context, scopeValue).asString() : "PLAYER";
            
            VariableScope scope = VariableScope.valueOf(scopeStr.toUpperCase());
            
            // For player scope, use the incrementPlayerVariable method
            if (scope == VariableScope.PLAYER) {
                variableManager.incrementPlayerVariable(player.getUniqueId(), variableName, increment);
            } else {
                // For other scopes, get the current value, increment it, and set it back
                DataValue currentValue = variableManager.getVariable(variableName, scope, player.getUniqueId().toString());
                double newValue = increment;
                if (currentValue != null) {
                    try {
                        newValue = currentValue.asNumber().doubleValue() + increment;
                    } catch (Exception e) {
                        // If current value is not a number, start from increment
                    }
                }
                DataValue newValueData = DataValue.fromObject(newValue);
                variableManager.setVariable(variableName, newValueData, scope, player.getUniqueId().toString());
            }
            
            return ExecutionResult.success("Variable '" + variableName + "' incremented by " + increment);
        } catch (Exception e) {
            return ExecutionResult.error("Error incrementing variable: " + e.getMessage());
        }
    }
    
    /**
     * Decrements a numeric variable
     */
    private ExecutionResult decrementVariable(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                            VariableManager variableManager, Player player) {
        try {
            DataValue variableNameValue = block.getParameter("variableName");
            DataValue decrementValue = block.getParameter("decrement");
            DataValue scopeValue = block.getParameter("scope");
            
            if (variableNameValue == null) {
                return ExecutionResult.error("Variable name parameter is required");
            }
            
            String variableName = resolver.resolve(context, variableNameValue).asString();
            double decrement = decrementValue != null ? resolver.resolve(context, decrementValue).asNumber().doubleValue() : 1.0;
            String scopeStr = scopeValue != null ? resolver.resolve(context, scopeValue).asString() : "PLAYER";
            
            VariableScope scope = VariableScope.valueOf(scopeStr.toUpperCase());
            
            // For player scope, use the incrementPlayerVariable method with negative value
            if (scope == VariableScope.PLAYER) {
                variableManager.incrementPlayerVariable(player.getUniqueId(), variableName, -decrement);
            } else {
                // For other scopes, get the current value, decrement it, and set it back
                DataValue currentValue = variableManager.getVariable(variableName, scope, player.getUniqueId().toString());
                double newValue = -decrement;
                if (currentValue != null) {
                    try {
                        newValue = currentValue.asNumber().doubleValue() - decrement;
                    } catch (Exception e) {
                        // If current value is not a number, start from -decrement
                    }
                }
                DataValue newValueData = DataValue.fromObject(newValue);
                variableManager.setVariable(variableName, newValueData, scope, player.getUniqueId().toString());
            }
            
            return ExecutionResult.success("Variable '" + variableName + "' decremented by " + decrement);
        } catch (Exception e) {
            return ExecutionResult.error("Error decrementing variable: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a variable
     */
    private ExecutionResult deleteVariable(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                         VariableManager variableManager, Player player) {
        try {
            DataValue variableNameValue = block.getParameter("variableName");
            DataValue scopeValue = block.getParameter("scope");
            
            if (variableNameValue == null) {
                return ExecutionResult.error("Variable name parameter is required");
            }
            
            String variableName = resolver.resolve(context, variableNameValue).asString();
            String scopeStr = scopeValue != null ? resolver.resolve(context, scopeValue).asString() : "PLAYER";
            
            VariableScope scope = VariableScope.valueOf(scopeStr.toUpperCase());
            
            // Use the removeVariable method from the interface
            variableManager.removeVariable(variableName, scope, player.getUniqueId().toString());
            
            return ExecutionResult.success("Variable '" + variableName + "' deleted successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Error deleting variable: " + e.getMessage());
        }
    }
    
    /**
     * Copies a variable from one name/scope to another
     */
    private ExecutionResult copyVariable(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                       VariableManager variableManager, Player player) {
        try {
            DataValue sourceNameValue = block.getParameter("sourceName");
            DataValue targetNameValue = block.getParameter("targetName");
            DataValue sourceScopeValue = block.getParameter("sourceScope");
            DataValue targetScopeValue = block.getParameter("targetScope");
            
            if (sourceNameValue == null || targetNameValue == null) {
                return ExecutionResult.error("Source name and target name parameters are required");
            }
            
            String sourceName = resolver.resolve(context, sourceNameValue).asString();
            String targetName = resolver.resolve(context, targetNameValue).asString();
            String sourceScopeStr = sourceScopeValue != null ? resolver.resolve(context, sourceScopeValue).asString() : "PLAYER";
            String targetScopeStr = targetScopeValue != null ? resolver.resolve(context, targetScopeValue).asString() : "PLAYER";
            
            VariableScope sourceScope = VariableScope.valueOf(sourceScopeStr.toUpperCase());
            VariableScope targetScope = VariableScope.valueOf(targetScopeStr.toUpperCase());
            
            DataValue value = variableManager.getVariable(sourceName, sourceScope, player.getUniqueId().toString());
            
            if (value == null) {
                return ExecutionResult.error("Source variable '" + sourceName + "' not found in " + sourceScope + " scope");
            }
            
            variableManager.setVariable(targetName, value, targetScope, player.getUniqueId().toString());
            
            return ExecutionResult.success("Variable '" + sourceName + "' copied to '" + targetName + "'");
        } catch (Exception e) {
            return ExecutionResult.error("Error copying variable: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a variable exists
     */
    private ExecutionResult variableExists(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                         VariableManager variableManager, Player player) {
        try {
            DataValue variableNameValue = block.getParameter("variableName");
            DataValue scopeValue = block.getParameter("scope");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (variableNameValue == null || resultVar == null) {
                return ExecutionResult.error("Variable name and result variable parameters are required");
            }
            
            String variableName = resolver.resolve(context, variableNameValue).asString();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            String scopeStr = scopeValue != null ? resolver.resolve(context, scopeValue).asString() : "PLAYER";
            
            VariableScope scope = VariableScope.valueOf(scopeStr.toUpperCase());
            boolean exists = variableManager.hasVariable(variableName, scope, player.getUniqueId().toString());
            
            context.setVariable(resultVariable, exists);
            
            if (exists) {
                return ExecutionResult.success("Variable '" + variableName + "' exists in " + scope + " scope");
            } else {
                return ExecutionResult.success("Variable '" + variableName + "' does not exist in " + scope + " scope");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error checking variable existence: " + e.getMessage());
        }
    }
}