package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for multiplying a variable by a value.
 * This action retrieves variable parameters from the container configuration and multiplies the variable by the value.
 */
public class MulVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get variable parameters from the container configuration
            MulVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
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
            VariableManager variableManager = context.getPlugin().getVariableManager();
            Player player = context.getPlayer();
            
            // Try to get the variable from different scopes
            DataValue currentVar = null;
            String scopeContext = null;
            VariableManager.VariableScope scope = null;
            
            // First try player variables
            if (player != null) {
                currentVar = variableManager.getPlayerVariable(player.getUniqueId(), varName);
                if (currentVar != null) {
                    scope = VariableManager.VariableScope.PLAYER;
                    scopeContext = player.getUniqueId().toString();
                }
            }
            
            // If not found, try local variables
            if (currentVar == null) {
                currentVar = variableManager.getLocalVariable(context.getScriptId(), varName);
                if (currentVar != null) {
                    scope = VariableManager.VariableScope.LOCAL;
                    scopeContext = context.getScriptId();
                }
            }
            
            // If not found, try global variables
            if (currentVar == null) {
                currentVar = variableManager.getGlobalVariable(varName);
                if (currentVar != null) {
                    scope = VariableManager.VariableScope.GLOBAL;
                    scopeContext = "global";
                }
            }
            
            // If not found, try server variables
            if (currentVar == null) {
                currentVar = variableManager.getServerVariable(varName);
                if (currentVar != null) {
                    scope = VariableManager.VariableScope.SERVER;
                    scopeContext = "server";
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
                    variableManager.setPlayerVariable(player.getUniqueId(), varName, newValueData);
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
    
    /**
     * Gets variable parameters from the container configuration
     */
    private MulVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        MulVarParams params = new MulVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
                    ItemStack nameItem = block.getConfigItem(nameSlot);
                    if (nameItem != null && nameItem.hasItemMeta()) {
                        // Extract variable name from item
                        params.nameStr = getVariableNameFromItem(nameItem);
                    }
                }
                
                // Get value from the value slot
                Integer valueSlot = slotResolver.apply("value");
                if (valueSlot != null) {
                    ItemStack valueItem = block.getConfigItem(valueSlot);
                    if (valueItem != null) {
                        // Extract value from item
                        params.valueStr = getValueFromItem(valueItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in MulVarAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts variable name from an item
     */
    private String getVariableNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the variable name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts value from an item
     */
    private String getValueFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the value
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        
        // If no display name, use the item amount as a number
        return String.valueOf(item.getAmount());
    }
    
    /**
     * Helper class to hold variable parameters
     */
    private static class MulVarParams {
        String nameStr = "";
        String valueStr = "";
    }
}