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
 * Action for getting a variable.
 * This action retrieves variable parameters from the container configuration and gets the variable.
 */
public class GetVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get variable parameters from the container configuration
            GetVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue targetValue = DataValue.of(params.targetStr);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String targetVar = resolvedTarget.asString();
            
            if (varName == null || varName.isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }

            if (targetVar == null || targetVar.isEmpty()) {
                return ExecutionResult.error("Invalid target variable");
            }

            // 🎆 ENHANCED: Actually get the variable using VariableManager
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("No player found in execution context");
            }
            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager not available");
            }
            
            // Get the variable for the player
            DataValue varValue = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (varValue == null) {
                return ExecutionResult.error("Variable '" + varName + "' not found");
            }
            
            // Set the target variable with the retrieved value
            variableManager.setPlayerVariable(player.getUniqueId(), targetVar, varValue);
            
            context.getPlugin().getLogger().info("💾 Variable retrieved: " + varName + " -> " + targetVar + " (value: " + varValue.asString() + ") for player " + player.getName());
            
            return ExecutionResult.success("Variable '" + varName + "' retrieved into '" + targetVar + "'");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private GetVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        GetVarParams params = new GetVarParams();
        
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
                
                // Get target variable from the target slot
                Integer targetSlot = slotResolver.apply("target");
                if (targetSlot != null) {
                    ItemStack targetItem = block.getConfigItem(targetSlot);
                    if (targetItem != null && targetItem.hasItemMeta()) {
                        // Extract target variable from item
                        params.targetStr = getTargetVariableFromItem(targetItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in GetVarAction: " + e.getMessage());
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
     * Extracts target variable from an item
     */
    private String getTargetVariableFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the target variable
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Helper class to hold variable parameters
     */
    private static class GetVarParams {
        String nameStr = "";
        String targetStr = "";
    }
}