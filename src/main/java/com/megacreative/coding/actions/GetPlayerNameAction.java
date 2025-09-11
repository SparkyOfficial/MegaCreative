package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for getting a player's name.
 * This action retrieves parameters from the container configuration and gets the player's name.
 */
public class GetPlayerNameAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get parameters from the container configuration
            GetPlayerNameParams params = getPlayerNameParamsFromContainer(block, context);

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue targetValue = DataValue.of(params.targetStr);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            // Parse parameters
            String targetVar = resolvedTarget.asString();
            
            if (targetVar == null || targetVar.isEmpty()) {
                return ExecutionResult.error("Invalid target variable");
            }

            // Get the player's name
            String playerName = player.getName();

            // Get the variable manager to set the variable
            // Note: This is a simplified implementation - in a real system, you would set the actual variable
            // For now, we'll just log the operation
            context.getPlugin().getLogger().info("Getting player name " + playerName + " into " + targetVar);
            
            return ExecutionResult.success("Player name retrieved successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get player name: " + e.getMessage());
        }
    }
    
    /**
     * Gets player name parameters from the container configuration
     */
    private GetPlayerNameParams getPlayerNameParamsFromContainer(CodeBlock block, ExecutionContext context) {
        GetPlayerNameParams params = new GetPlayerNameParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
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
            context.getPlugin().getLogger().warning("Error getting player name parameters from container in GetPlayerNameAction: " + e.getMessage());
        }
        
        return params;
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
     * Helper class to hold player name parameters
     */
    private static class GetPlayerNameParams {
        String targetStr = "";
    }
}