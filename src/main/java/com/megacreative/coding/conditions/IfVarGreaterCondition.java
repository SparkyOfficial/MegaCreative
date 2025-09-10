package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;

/**
 * Условие для проверки, что переменная больше заданного значения.
 */
public class IfVarGreaterCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawVar = block.getParameter("var");
        DataValue rawValue = block.getParameter("value");

        if (rawVar == null || rawValue == null) {
            context.getPlugin().getLogger().warning("Variable or value not specified in IfVarGreaterCondition");
            return false;
        }

        DataValue varName = resolver.resolve(context, rawVar);
        DataValue compareValue = resolver.resolve(context, rawValue);
        
        try {
            // Get the actual variable value from VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Try to get the variable from different scopes
                DataValue actualVarValue = null;
                
                // Try player scope first if we have a player
                if (context.getPlayer() != null) {
                    actualVarValue = variableManager.getVariable(varName.asString(), VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
                }
                
                // Try local scope if we have a script context
                if (actualVarValue == null && context.getScriptId() != null) {
                    actualVarValue = variableManager.getVariable(varName.asString(), VariableScope.LOCAL, context.getScriptId());
                }
                
                // Try global scope
                if (actualVarValue == null) {
                    actualVarValue = variableManager.getVariable(varName.asString(), VariableScope.GLOBAL, "global");
                }
                
                // Try server scope
                if (actualVarValue == null) {
                    actualVarValue = variableManager.getVariable(varName.asString(), VariableScope.SERVER, "server");
                }
                
                if (actualVarValue != null) {
                    // Try to compare as numbers
                    double varNum = Double.parseDouble(actualVarValue.asString());
                    double compareNum = Double.parseDouble(compareValue.asString());
                    return varNum > compareNum;
                }
            }
        } catch (NumberFormatException e) {
            // If not numbers, compare as strings length
            context.getPlugin().getLogger().warning("Failed to parse numbers in IfVarGreaterCondition: " + e.getMessage());
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error in IfVarGreaterCondition: " + e.getMessage());
        }
        
        return false;
    }
}