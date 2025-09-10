package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for waiting/pausing execution for a specified amount of time.
 * This action pauses the script execution for a specified number of ticks from container configuration.
 */
public class WaitAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get duration from the container configuration
            int ticks = getDurationFromContainer(block, context);
            
            if (ticks <= 0) {
                return ExecutionResult.error("Invalid duration value");
            }

            // Pause execution for the specified number of ticks
            // Note: This is a simplified implementation. In a real implementation,
            // you would need to handle asynchronous execution properly.
            try {
                Thread.sleep(ticks * 50); // 1 tick = 50ms in Minecraft
                return ExecutionResult.success("Waited for " + ticks + " ticks");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ExecutionResult.error("Wait was interrupted");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to wait: " + e.getMessage());
        }
    }
    
    /**
     * Gets duration from the container configuration
     */
    private int getDurationFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get duration from the duration slot
                Integer durationSlot = slotResolver.apply("duration_slot");
                if (durationSlot != null) {
                    ItemStack durationItem = block.getConfigItem(durationSlot);
                    if (durationItem != null && durationItem.hasItemMeta()) {
                        // Extract duration from item
                        return getDurationFromItem(durationItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting duration from container in WaitAction: " + e.getMessage());
        }
        
        return 1; // Default to 1 tick
    }
    
    /**
     * Extracts duration from an item
     */
    private int getDurationFromItem(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse duration from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Math.max(1, Integer.parseInt(cleanName));
                }
            }
            
            // Fallback to item amount
            return Math.max(1, item.getAmount());
        } catch (Exception e) {
            return 1; // Default to 1 tick
        }
    }
}