package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import com.megacreative.services.BlockConfigService;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a variable equals a specific value.
 * This condition returns true if the variable's value equals the specified value from container configuration.
 */
public class IfVarEqualsCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters from the container configuration
            IfVarEqualsParams params = getVarParamsFromContainer(block, context);
            
            if (params.varName == null || params.varName.isEmpty()) {
                return false;
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedVarName = resolver.resolveString(context, params.varName);
            DataValue resolvedValue = resolver.resolve(context, params.value);

            // Get the variable using the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Try to get the variable from different scopes
                DataValue variableValue = null;
                
                // Try player scope first if we have a player
                if (context.getPlayer() != null) {
                    variableValue = variableManager.getVariable(resolvedVarName, VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
                }
                
                // Try local scope if we have a script context
                if (variableValue == null && context.getScriptId() != null) {
                    variableValue = variableManager.getVariable(resolvedVarName, VariableScope.LOCAL, context.getScriptId());
                }
                
                // Try global scope
                if (variableValue == null) {
                    variableValue = variableManager.getVariable(resolvedVarName, VariableScope.GLOBAL, "global");
                }
                
                // Try server scope
                if (variableValue == null) {
                    variableValue = variableManager.getVariable(resolvedVarName, VariableScope.SERVER, "server");
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
    
    /**
     * Gets variable parameters from the container configuration
     */
    private IfVarEqualsParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IfVarEqualsParams params = new IfVarEqualsParams();
        
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
                        params.varName = getVarNameFromItem(nameItem);
                    }
                }
                
                // Get value from the value slot
                Integer valueSlot = slotResolver.apply("value");
                if (valueSlot != null) {
                    ItemStack valueItem = block.getConfigItem(valueSlot);
                    if (valueItem != null) {
                        // Extract value from item
                        params.value = getValueFromItem(valueItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in IfVarEqualsCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts variable name from an item
     */
    private String getVarNameFromItem(ItemStack item) {
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
     * In a real implementation, this would parse the value based on the item type
     * For now, we'll create a simple string value
     */
    private DataValue getValueFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the value
                String cleanValue = displayName.replaceAll("[§0-9]", "").trim();
                return DataValue.of(cleanValue);
            }
        }
        
        // Fallback to item type
        return DataValue.of(item.getType().name());
    }
    
    /**
     * Helper class to hold variable parameters
     */
    private static class IfVarEqualsParams {
        String varName = "";
        DataValue value = DataValue.of("");
    }
}