package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;

/**
 * Condition for comparing values of variables.
 */
public class CompareVariableCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        ParameterResolver resolver = new ParameterResolver(context);

        // Get and resolve parameters
        DataValue rawVar1 = block.getParameter("var1");
        DataValue rawVar2 = block.getParameter("var2");
        DataValue rawOperator = block.getParameter("operator");

        if (rawVar1 == null || rawVar2 == null) {
            context.getPlugin().getLogger().warning("Variables not specified in CompareVariableCondition");
            return false;
        }

        DataValue var1Name = resolver.resolve(context, rawVar1);
        DataValue var2Name = resolver.resolve(context, rawVar2);
        DataValue operatorValue = rawOperator != null ? resolver.resolve(context, rawOperator) : null;

        String operator = operatorValue != null ? operatorValue.asString() : "==";
        
        try {
            // Get the actual variable values from VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Get first variable value
                DataValue var1Value = getVariableValue(variableManager, var1Name.asString(), context);
                if (var1Value == null) {
                    context.getPlugin().getLogger().warning("Variable '" + var1Name.asString() + "' not found in CompareVariableCondition");
                    return false;
                }
                
                // Get second variable value
                DataValue var2Value = getVariableValue(variableManager, var2Name.asString(), context);
                if (var2Value == null) {
                    context.getPlugin().getLogger().warning("Variable '" + var2Name.asString() + "' not found in CompareVariableCondition");
                    return false;
                }
                
                // Convert both values to strings for comparison
                String value1 = var1Value.asString();
                String value2 = var2Value.asString();
                
                // Try to compare as numbers if possible
                try {
                    double num1 = Double.parseDouble(value1);
                    double num2 = Double.parseDouble(value2);
                    
                    switch (operator) {
                        case ">":
                            return num1 > num2;
                        case ">=":
                            return num1 >= num2;
                        case "<":
                            return num1 < num2;
                        case "<=":
                            return num1 <= num2;
                        case "!=":
                            return num1 != num2;
                        case "==":
                        default:
                            return num1 == num2;
                    }
                } catch (NumberFormatException e) {
                    // If not numbers, compare as strings
                    switch (operator) {
                        case "!=":
                            return !value1.equals(value2);
                        case "==":
                        default:
                            return value1.equals(value2);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error in CompareVariableCondition: " + e.getMessage());
            return false;
        }
        
        return false;
    }
    
    private DataValue getVariableValue(VariableManager variableManager, String varName, ExecutionContext context) {
        // Try to get the variable from different scopes
        DataValue varValue = null;
        
        // Try player scope first if we have a player
        if (context.getPlayer() != null) {
            varValue = variableManager.getVariable(varName, VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
        }
        
        // Try local scope if we have a script context
        if (varValue == null && context.getScriptId() != null) {
            varValue = variableManager.getVariable(varName, VariableScope.LOCAL, context.getScriptId());
        }
        
        // Try global scope
        if (varValue == null) {
            varValue = variableManager.getVariable(varName, VariableScope.GLOBAL, "global");
        }
        
        // Try server scope
        if (varValue == null) {
            varValue = variableManager.getVariable(varName, VariableScope.SERVER, "server");
        }
        
        return varValue;
    }
}