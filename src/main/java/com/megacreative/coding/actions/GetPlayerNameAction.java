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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for getting a player's name.
 * This action retrieves the player's name and stores it in a variable from container configuration.
 */
public class GetPlayerNameAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the target variable name from the container configuration
            String targetName = getTargetNameFromContainer(block, context);
            if (targetName == null || targetName.isEmpty()) {
                return ExecutionResult.error("Target variable name is not configured");
            }

            // Resolve any placeholders in the target variable name
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedTargetName = resolver.resolveString(context, targetName);

            // Get the player's name
            String playerName = player.getName();
            
            // Store the player's name in the target variable
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                String scriptId = context.getScriptId() != null ? context.getScriptId() : "global";
                variableManager.setVariable(resolvedTargetName, DataValue.of(playerName), VariableScope.LOCAL, scriptId);
                return ExecutionResult.success("Player name '" + playerName + "' stored in '" + resolvedTargetName + "'");
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get player name: " + e.getMessage());
        }
    }
    
    /**
     * Gets target variable name from the container configuration
     */
    private String getTargetNameFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get target from the target slot
                Integer targetSlot = slotResolver.apply("target");
                if (targetSlot != null) {
                    ItemStack targetItem = block.getConfigItem(targetSlot);
                    if (targetItem != null && targetItem.hasItemMeta()) {
                        // Extract target variable name from item
                        return getTargetNameFromItem(targetItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting target variable name from container in GetPlayerNameAction: " + e.getMessage());
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
}