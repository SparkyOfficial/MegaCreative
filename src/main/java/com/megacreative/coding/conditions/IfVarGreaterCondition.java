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
 * Condition for checking if a variable is greater than a specific value from the new parameter system.
 * This condition returns true if the specified variable is greater than the specified value.
 */
@BlockMeta(id = "ifVarGreater", displayName = "§aIf Variable Greater", type = BlockType.CONDITION)
public class IfVarGreaterCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            
            DataValue nameValue = block.getParameter("name");
            DataValue valueValue = block.getParameter("value");
            
            if (nameValue == null || nameValue.isEmpty()) {
                context.getPlugin().getLogger().warning("IfVarGreaterCondition: 'name' parameter is missing.");
                return false;
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                context.getPlugin().getLogger().warning("IfVarGreaterCondition: 'value' parameter is missing.");
                return false;
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            
            String varName = resolvedName.asString();
            String compareValueStr = resolvedValue.asString();
            
            if (varName == null || varName.isEmpty() || compareValueStr == null || compareValueStr.isEmpty()) {
                context.getPlugin().getLogger().warning("IfVarGreaterCondition: One or more parameters are empty.");
                return false;
            }

            
            double compareValue;
            try {
                compareValue = Double.parseDouble(compareValueStr);
            } catch (NumberFormatException e) {
                context.getPlugin().getLogger().warning("IfVarGreaterCondition: Invalid value '" + compareValueStr + "' for comparison.");
                return false;
            }

            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            
            
            DataValue varValueData = null;
            
            
            if (varValueData == null) {
                varValueData = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            }
            
            
            if (varValueData == null) {
                varValueData = variableManager.getLocalVariable(context.getScriptId(), varName);
            }
            
            
            if (varValueData == null) {
                varValueData = variableManager.getGlobalVariable(varName);
            }
            
            
            if (varValueData == null) {
                varValueData = variableManager.getServerVariable(varName);
            }
            
            
            if (varValueData == null) {
                context.getPlugin().getLogger().warning("IfVarGreaterCondition: Variable '" + varName + "' not found.");
                return false;
            }
            
            
            double varValue;
            try {
                varValue = varValueData.asNumber().doubleValue();
            } catch (NumberFormatException e) {
                context.getPlugin().getLogger().warning("IfVarGreaterCondition: Variable '" + varName + "' is not a valid number.");
                return false;
            }

            
            return varValue > compareValue;
        } catch (Exception e) {
            
            context.getPlugin().getLogger().warning("Error in IfVarGreaterCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold variable parameters
     */
    private static class IfVarGreaterParams {
        String nameStr = "";
        String valueStr = "";
    }
}