package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a variable is less than a specific value from the new parameter system.
 * This condition returns true if the specified variable is less than the specified value.
 */
@BlockMeta(id = "ifVarLess", displayName = "§aIf Variable Less", type = BlockType.CONDITION)
public class IfVarLessCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the new parameter system
            DataValue nameValue = block.getParameter("name");
            DataValue valueValue = block.getParameter("value");
            
            if (nameValue == null || nameValue.isEmpty()) {
                context.getPlugin().getLogger().warning("IfVarLessCondition: 'name' parameter is missing.");
                return false;
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                context.getPlugin().getLogger().warning("IfVarLessCondition: 'value' parameter is missing.");
                return false;
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String compareValueStr = resolvedValue.asString();
            
            if (varName == null || varName.isEmpty() || compareValueStr == null || compareValueStr.isEmpty()) {
                context.getPlugin().getLogger().warning("IfVarLessCondition: One or more parameters are empty.");
                return false;
            }

            // Parse the comparison value as a number
            double compareValue;
            try {
                compareValue = Double.parseDouble(compareValueStr);
            } catch (NumberFormatException e) {
                context.getPlugin().getLogger().warning("IfVarLessCondition: Invalid value '" + compareValueStr + "' for comparison.");
                return false;
            }

            // Get the variable manager to retrieve the variable value
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            
            // Try to get the variable value from different scopes
            DataValue varValueData = null;
            
            // Try player variables first
            if (varValueData == null) {
                varValueData = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            }
            
            // Try local variables
            if (varValueData == null) {
                varValueData = variableManager.getLocalVariable(context.getScriptId(), varName);
            }
            
            // Try global variables
            if (varValueData == null) {
                varValueData = variableManager.getGlobalVariable(varName);
            }
            
            // Try server variables
            if (varValueData == null) {
                varValueData = variableManager.getServerVariable(varName);
            }
            
            // If we couldn't find the variable, return false
            if (varValueData == null) {
                context.getPlugin().getLogger().warning("IfVarLessCondition: Variable '" + varName + "' not found.");
                return false;
            }
            
            // Parse the variable value as a number
            double varValue;
            try {
                varValue = varValueData.asNumber().doubleValue();
            } catch (NumberFormatException e) {
                context.getPlugin().getLogger().warning("IfVarLessCondition: Variable '" + varName + "' is not a valid number.");
                return false;
            }

            // Compare the variable value with the specified value
            return varValue < compareValue;
        } catch (Exception e) {
            // If there's an error, return false
            context.getPlugin().getLogger().warning("Error in IfVarLessCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold variable parameters
     */
    private static class IfVarLessParams {
        String nameStr = "";
        String valueStr = "";
    }
}