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
 * Action for getting a global variable.
 * This action retrieves a global variable and stores it in another variable or context from container configuration.
 */
public class GetGlobalVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters from the container configuration
            GetGlobalVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.varName == null || params.varName.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }
            
            if (params.targetName == null || params.targetName.isEmpty()) {
                return ExecutionResult.error("Target variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedVarName = resolver.resolveString(context, params.varName);
            String resolvedTargetName = resolver.resolveString(context, params.targetName);

            // Get the variable using the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Use global scope
                DataValue value = variableManager.getVariable(resolvedVarName, VariableScope.GLOBAL, "global");
                if (value != null) {
                    // Store in target variable (using local scope by default)
                    String scriptId = context.getScriptId() != null ? context.getScriptId() : "global";
                    variableManager.setVariable(resolvedTargetName, value, VariableScope.LOCAL, scriptId);
                    return ExecutionResult.success("Global variable '" + resolvedVarName + "' retrieved and stored in '" + resolvedTargetName + "' successfully");
                } else {
                    return ExecutionResult.error("Global variable '" + resolvedVarName + "' not found");
                }
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get global variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private GetGlobalVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        GetGlobalVarParams params = new GetGlobalVarParams();
        
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
                
                // Get target variable name from the target slot
                Integer targetSlot = slotResolver.apply("target");
                if (targetSlot != null) {
                    ItemStack targetItem = block.getConfigItem(targetSlot);
                    if (targetItem != null && targetItem.hasItemMeta()) {
                        // Extract target variable name from item
                        params.targetName = getTargetNameFromItem(targetItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in GetGlobalVarAction: " + e.getMessage());
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
     * Extracts target variable name from an item
     */
    private String getTargetNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the target variable name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Helper class to hold variable parameters
     */
    private static class GetGlobalVarParams {
        String varName = "";
        String targetName = "";
    }
}