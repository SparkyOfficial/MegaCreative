package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a variable is less than a specific value from container configuration.
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
            // Get parameters from the container configuration
            IfVarLessParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return false;
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String compareValueStr = resolvedValue.asString();
            
            if (varName == null || varName.isEmpty() || compareValueStr == null || compareValueStr.isEmpty()) {
                return false;
            }

            // Parse the comparison value as a number
            double compareValue;
            try {
                compareValue = Double.parseDouble(compareValueStr);
            } catch (NumberFormatException e) {
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
                return false;
            }
            
            // Parse the variable value as a number
            double varValue;
            try {
                varValue = varValueData.asNumber().doubleValue();
            } catch (NumberFormatException e) {
                return false;
            }

            // Compare the variable value with the specified value
            return varValue < compareValue;
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private IfVarLessParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IfVarLessParams params = new IfVarLessParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
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
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in IfVarLessCondition: " + e.getMessage());
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
    private static class IfVarLessParams {
        String nameStr = "";
        String valueStr = "";
    }
}