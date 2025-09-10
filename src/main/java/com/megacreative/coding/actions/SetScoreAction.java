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
 * Action for setting a score on a scoreboard.
 * This action sets a score for a specific key on the player's scoreboard from container configuration.
 */
public class SetScoreAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get parameters from the container configuration
            SetScoreParams params = getScoreParamsFromContainer(block, context);
            
            if (params.key == null || params.key.isEmpty()) {
                return ExecutionResult.error("Key is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedKey = resolver.resolveString(context, params.key);

            // Set the score
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getGameScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.setPlayerScore(player, resolvedKey, params.value);
                return ExecutionResult.success("Set score for '" + resolvedKey + "' to " + params.value);
            } else {
                return ExecutionResult.error("Scoreboard manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set score: " + e.getMessage());
        }
    }
    
    /**
     * Gets score parameters from the container configuration
     */
    private SetScoreParams getScoreParamsFromContainer(CodeBlock block, ExecutionContext context) {
        SetScoreParams params = new SetScoreParams();
        
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
                
                // Get value from the value slot
                Integer valueSlot = slotResolver.apply("value");
                if (valueSlot != null) {
                    ItemStack valueItem = block.getConfigItem(valueSlot);
                    if (valueItem != null && valueItem.hasItemMeta()) {
                        // Extract value from item
                        params.value = getValueFromItem(valueItem, 0);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting score parameters from container in SetScoreAction: " + e.getMessage());
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
     * Extracts value from an item
     */
    private int getValueFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse value from display name (e.g., "value:10")
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
     * Helper class to hold score parameters
     */
    private static class SetScoreParams {
        String key = "";
        int value = 0;
    }
}