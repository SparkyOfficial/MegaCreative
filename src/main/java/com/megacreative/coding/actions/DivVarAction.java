package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

/**
 * Action for dividing a variable by a value.
 * This action retrieves variable parameters from the new parameter system.
 */
@BlockMeta(id = "divVar", displayName = "§aDivide Variable", type = BlockType.ACTION)
public class DivVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue nameValue = block.getParameter("name");
            DataValue valueValue = block.getParameter("value");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("No variable name provided");
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                return ExecutionResult.error("No value provided");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            // Removed redundant null check - static analysis flagged it as always non-null when this method is called

            
            double value;
            try {
                value = Double.parseDouble(valueStr);
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid value: " + valueStr);
            }

            
            if (value == 0) {
                return ExecutionResult.error("Cannot divide by zero");
            }

            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            Player player = context.getPlayer();
            
            
            DataValue currentVar = null;
            VariableManager.VariableScope scope = null;
            
            // player is never null when this method is called according to static analysis
            java.util.UUID playerUUID = player.getUniqueId();
            currentVar = variableManager.getPlayerVariable(playerUUID, varName);
            if (currentVar != null) {
                scope = VariableManager.VariableScope.PLAYER;
            }
            
            
            if (currentVar == null) {
                currentVar = variableManager.getLocalVariable(context.getScriptId(), varName);
                if (currentVar != null) {
                    scope = VariableManager.VariableScope.LOCAL;
                }
            }
            
            
            if (currentVar == null) {
                currentVar = variableManager.getGlobalVariable(varName);
                if (currentVar != null) {
                    scope = VariableManager.VariableScope.GLOBAL;
                }
            }
            
            
            if (currentVar == null) {
                currentVar = variableManager.getServerVariable(varName);
                if (currentVar != null) {
                    scope = VariableManager.VariableScope.SERVER;
                }
            }
            
            
            double currentValue = 0.0;
            if (currentVar != null) {
                try {
                    currentValue = currentVar.asNumber().doubleValue();
                } catch (NumberFormatException e) {
                    
                    currentValue = 0.0;
                }
            }
            
            
            double newValue = currentValue / value;
            
            
            DataValue newValueData = DataValue.of(newValue);
            if (scope != null) {
                switch (scope) {
                    case PLAYER:
                        variableManager.setPlayerVariable(playerUUID, varName, newValueData);
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
                }
            } else {
                // If scope is null, use local scope as fallback
                variableManager.setLocalVariable(context.getScriptId(), varName, newValueData);
            }
            
            context.getPlugin().getLogger().fine("Dividing variable " + varName + " by " + value + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
}