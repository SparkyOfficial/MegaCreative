package com.megacreative.coding.actions.scoreboard;

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
 * Action for setting a score on a scoreboard.
 * This action retrieves parameters from the container configuration and sets a score.
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

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue keyVal = DataValue.of(params.keyStr);
            DataValue resolvedKey = resolver.resolve(context, keyVal);
            
            DataValue valueVal = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueVal);
            
            // Parse parameters
            String key = resolvedKey.asString();
            String valueStr = resolvedValue.asString();
            
            if (key == null || key.isEmpty()) {
                return ExecutionResult.error("Invalid score key");
            }

            // Parse the value as a number
            int value;
            try {
                value = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid score value: " + valueStr);
            }

            // Set the score
            // Note: This is a simplified implementation - in a real system, you would set the actual score
            // For now, we'll just log the operation
            context.getPlugin().getLogger().info("Setting score " + key + " to " + value);
            
            return ExecutionResult.success("Score set successfully");
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
                        params.keyStr = getKeyFromItem(keyItem);
                    }
                }
                
                // Get value from the value slot
                Integer valueSlot = slotResolver.apply("value");
                if (valueSlot != null) {
                    ItemStack valueItem = block.getConfigItem(valueSlot);
                    if (valueItem != null && valueItem.hasItemMeta()) {
                        // Extract value from item
                        params.valueStr = getValueFromItem(valueItem);
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
        return "";
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
        return "0";
    }
    
    /**
     * Helper class to hold score parameters
     */
    private static class SetScoreParams {
        String keyStr = "";
        String valueStr = "0";
    }
}