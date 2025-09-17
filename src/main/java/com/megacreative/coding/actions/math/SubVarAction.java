package com.megacreative.coding.actions.math;

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
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
public class SubVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
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

            // Get the variable manager to update the variable
            VariableManager variableManager = context.getPlugin().getVariableManager();
            
            // Try to get the current variable value
            DataValue currentVarValue = null;
            
            // Try player variables first
            if (currentVarValue == null) {
                currentVarValue = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            }
            
            // Try local variables
            if (currentVarValue == null) {
                currentVarValue = variableManager.getLocalVariable(context.getScriptId(), varName);
            }
            
            // Try global variables
            if (currentVarValue == null) {
                currentVarValue = variableManager.getGlobalVariable(varName);
            }
            
            // Try server variables
            if (currentVarValue == null) {
                currentVarValue = variableManager.getServerVariable(varName);
            }
            
            // Get the current value or default to 0
            double currentValue = 0.0;
            if (currentVarValue != null && currentVarValue.getType().isNumber()) {
                try {
                    currentValue = currentVarValue.asNumber().doubleValue();
                } catch (Exception e) {
                    // If conversion fails, keep default value of 0
                }
            }
            
            // Calculate the new value
            double newValue = currentValue - value;
            
            // Update the variable with the new value
            DataValue newValueData = DataValue.of(newValue);
            
            // Set the variable in the appropriate scope
            // For now, we'll set it as a player variable, but this should be configurable
            variableManager.setPlayerVariable(player.getUniqueId(), varName, newValueData);
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        SubVarParams params = new SubVarParams();
        
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
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in SubVarAction: " + e.getMessage());
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
    private static class SubVarParams {
        String nameStr = "";
        String valueStr = "";
    }
}