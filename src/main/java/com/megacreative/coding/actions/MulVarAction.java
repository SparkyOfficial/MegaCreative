package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

/**
 * Action for multiplying a variable by a value.
 * This action retrieves variable parameters from the new parameter system and multiplies the variable by the value.
 */
public class MulVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters from the new parameter system
            DataValue nameValue = block.getParameter("name");
            DataValue valueValue = block.getParameter("value");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("No variable name provided");
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                return ExecutionResult.error("No value provided");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }

            // Parse the value as a number
            double value;
            try {
                value = Double.parseDouble(valueStr);
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            Player player = context.getPlayer();
            
            // Try to get the variable from different scopes
            DataValue currentVar = null;
            VariableManager.VariableScope scope = null;
            
            // First try player variables
            if (player != null) {
                java.util.UUID playerUUID = player.getUniqueId();
                if (playerUUID != null) {
                    currentVar = variableManager.getPlayerVariable(playerUUID, varName);
                    if (currentVar != null) {
                        scope = VariableManager.VariableScope.PLAYER;
                    }
                }
            }
            
            // If not found, try local variables
            if (currentVar == null) {
                currentVar = variableManager.getLocalVariable(context.getScriptId(), varName);
                if (currentVar != null) {
                    scope = VariableManager.VariableScope.LOCAL;
                }
            }
            
            // If not found, try global variables
            if (currentVar == null) {
                currentVar = variableManager.getGlobalVariable(varName);
                if (currentVar != null) {
                    scope = VariableManager.VariableScope.GLOBAL;
                }
            }
            
            // If not found, try server variables
            if (currentVar == null) {
                currentVar = variableManager.getServerVariable(varName);
                if (currentVar != null) {
                    scope = VariableManager.VariableScope.SERVER;
                }
            }
            
            // Get current value or default to 0
            double currentValue = 0.0;
            if (currentVar != null) {
                try {
                    currentValue = currentVar.asNumber().doubleValue();
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0
                    currentValue = 0.0;
                }
            }
            
            // Calculate new value
            double newValue = currentValue * value;
            
            // Set the updated value based on the scope
            DataValue newValueData = DataValue.of(newValue);
            switch (scope) {
                case PLAYER:
                    if (player != null) {
                        java.util.UUID playerUUID = player.getUniqueId();
                        if (playerUUID != null) {
                            variableManager.setPlayerVariable(playerUUID, varName, newValueData);
                        }
                    }
                    break;
                case LOCAL:
                    variableManager.setLocalVariable(context.getScriptId(), varName, newValueData);
                    break;
                case GLOBAL:
                    variableManager.setGlobalVariable(varName, newValueData);
                    break;
                case SERVER:
                    variableManager.setServerVariable(varName, newValueData);
                    break;
                default:
                    // If variable doesn't exist, create it as a local variable
                    variableManager.setLocalVariable(context.getScriptId(), varName, newValueData);
                    break;
            }
            
            context.getPlugin().getLogger().info("Multiplying variable " + varName + " by " + value + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
}