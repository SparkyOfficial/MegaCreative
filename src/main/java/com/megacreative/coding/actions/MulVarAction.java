package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;

/**
 * Action for multiplying a variable by a value.
 * This action multiplies an existing variable by a value.
 */
public class MulVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get the variable name parameter from the block
            DataValue nameValue = block.getParameter("name");
            if (nameValue == null) {
                return ExecutionResult.error("Variable name parameter is missing");
            }

            // Get the value parameter from the block
            DataValue valueValue = block.getParameter("value");
            if (valueValue == null) {
                return ExecutionResult.error("Value parameter is missing");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            if (varName == null || varName.isEmpty()) {
                return ExecutionResult.error("Variable name is empty or null");
            }

            // Get the variable using the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Try to get the variable from different scopes
                DataValue variableValue = null;
                VariableScope variableScope = VariableScope.LOCAL;
                String variableContext = context.getScriptId() != null ? context.getScriptId() : "global";
                
                // Try player scope first if we have a player
                if (context.getPlayer() != null) {
                    variableValue = variableManager.getVariable(varName, VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
                    if (variableValue != null) {
                        variableScope = VariableScope.PLAYER;
                        variableContext = context.getPlayer().getUniqueId().toString();
                    }
                }
                
                // Try local scope if we have a script context
                if (variableValue == null && context.getScriptId() != null) {
                    variableValue = variableManager.getVariable(varName, VariableScope.LOCAL, context.getScriptId());
                    if (variableValue != null) {
                        variableScope = VariableScope.LOCAL;
                        variableContext = context.getScriptId();
                    }
                }
                
                // Try global scope
                if (variableValue == null) {
                    variableValue = variableManager.getVariable(varName, VariableScope.GLOBAL, "global");
                    if (variableValue != null) {
                        variableScope = VariableScope.GLOBAL;
                        variableContext = "global";
                    }
                }
                
                // Try server scope
                if (variableValue == null) {
                    variableValue = variableManager.getVariable(varName, VariableScope.SERVER, "server");
                    if (variableValue != null) {
                        variableScope = VariableScope.SERVER;
                        variableContext = "server";
                    }
                }
                
                if (variableValue != null) {
                    try {
                        // Try to multiply as numbers
                        double varNum = Double.parseDouble(variableValue.asString());
                        double mulNum = Double.parseDouble(resolvedValue.asString());
                        double result = varNum * mulNum;
                        
                        // Set the new value
                        variableManager.setVariable(varName, DataValue.of(result), variableScope, variableContext);
                        return ExecutionResult.success("Multiplied variable '" + varName + "' by " + mulNum + ", result: " + result);
                    } catch (NumberFormatException e) {
                        return ExecutionResult.error("Cannot multiply non-numeric variable '" + varName + "'");
                    }
                } else {
                    return ExecutionResult.error("Variable '" + varName + "' not found");
                }
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to multiply variable: " + e.getMessage());
        }
    }
}