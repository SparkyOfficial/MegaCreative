package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import com.megacreative.services.BlockConfigService;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for setting a global variable.
 * This action sets a global variable to a specified value from container configuration.
 */
public class SetGlobalVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters from the container configuration
            SetGlobalVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.varName == null || params.varName.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedVarName = resolver.resolveString(context, params.varName);
            DataValue resolvedValue = resolver.resolve(context, params.value);

            // Set the variable using the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Use global scope
                variableManager.setVariable(resolvedVarName, resolvedValue, VariableScope.GLOBAL, "global");
                return ExecutionResult.success("Global variable '" + resolvedVarName + "' set successfully");
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set global variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SetGlobalVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        SetGlobalVarParams params = new SetGlobalVarParams();
        
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
                        params.varName = getVarNameFromItem(nameItem);
                    }
                }
                
                // Get variable value from the value slot
                Integer valueSlot = slotResolver.apply("value");
                if (valueSlot != null) {
                    ItemStack valueItem = block.getConfigItem(valueSlot);
                    if (valueItem != null) {
                        // Extract variable value from item
                        params.value = getValueFromItem(valueItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in SetGlobalVarAction: " + e.getMessage());
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
     * Extracts variable value from an item
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
    private static class SetGlobalVarParams {
        String varName = "";
        DataValue value = DataValue.of("");
    }
}