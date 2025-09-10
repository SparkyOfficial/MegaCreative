package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;

/**
 * Condition for checking if a variable equals a specific value.
 * This condition returns true if the variable's value equals the specified value.
 */
public class IfVarEqualsCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        try {
            // Get the variable name parameter from the block
            DataValue nameValue = block.getParameter("name");
            if (nameValue == null) {
                return false;
            }

            // Get the value parameter from the block
            DataValue valueValue = block.getParameter("value");
            if (valueValue == null) {
                return false;
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            if (varName == null || varName.isEmpty()) {
                return false;
            }

            // Get the variable using the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Try to get the variable from different scopes
                DataValue variableValue = null;
                
                // Try player scope first if we have a player
                if (context.getPlayer() != null) {
                    variableValue = variableManager.getVariable(varName, VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
                }
                
                // Try local scope if we have a script context
                if (variableValue == null && context.getScriptId() != null) {
                    variableValue = variableManager.getVariable(varName, VariableScope.LOCAL, context.getScriptId());
                }
                
                // Try global scope
                if (variableValue == null) {
                    variableValue = variableManager.getVariable(varName, VariableScope.GLOBAL, "global");
                }
                
                // Try server scope
                if (variableValue == null) {
                    variableValue = variableManager.getVariable(varName, VariableScope.SERVER, "server");
                }
                
                if (variableValue != null) {
                    // Compare the values
                    return variableValue.asString().equals(resolvedValue.asString());
                }
            }
            
            return false;
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}