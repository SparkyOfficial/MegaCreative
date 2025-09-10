package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.managers.GameScoreboardManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for incrementing a score on a scoreboard.
 * This action increments a score for a specific key on the player's scoreboard from container configuration.
 */
public class IncrementScoreAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get parameters from the container configuration
            IncrementScoreParams params = getIncrementParamsFromContainer(block, context);
            
            if (params.key == null || params.key.isEmpty()) {
                return ExecutionResult.error("Key is not configured");
            }

            // Resolve any placeholders in the key
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedKey = resolver.resolveString(context, params.key);

            // Increment the score
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getGameScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.incrementPlayerScore(player, resolvedKey, params.increment);
                return ExecutionResult.success("Incremented score for '" + resolvedKey + "' by " + params.increment);
            } else {
                return ExecutionResult.error("Scoreboard manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to increment score: " + e.getMessage());
        }
    }
    
    /**
     * Gets increment parameters from the container configuration
     */
    private IncrementScoreParams getIncrementParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IncrementScoreParams params = new IncrementScoreParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get key from the key slot
                Integer keySlot = slotResolver.apply("key");
                if (keySlot != null) {
                    ItemStack keyItem = block.getConfigItem(keySlot);
                    if (keyItem != null && keyItem.hasItemMeta()) {
                        // Extract key from item
                        params.key = getKeyFromItem(keyItem);
                    }
                }
                
                // Get increment from the increment slot
                Integer incrementSlot = slotResolver.apply("increment");
                if (incrementSlot != null) {
                    ItemStack incrementItem = block.getConfigItem(incrementSlot);
                    if (incrementItem != null && incrementItem.hasItemMeta()) {
                        // Extract increment from item
                        params.increment = getIncrementFromItem(incrementItem, 1);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting increment parameters from container in IncrementScoreAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts key from an item
     */
    private String getKeyFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the key
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts increment from an item
     */
    private int getIncrementFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse increment from display name (e.g., "increment:5")
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    if (cleanName.contains(":")) {
                        String[] parts = cleanName.split(":");
                        if (parts.length > 1) {
                            return Integer.parseInt(parts[1].trim());
                        }
                    }
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Helper class to hold increment parameters
     */
    private static class IncrementScoreParams {
        String key = "";
        int increment = 1;
    }
}