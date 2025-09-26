package com.megacreative.coding.actions.world;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for setting the time in a world.
 * This action changes the world time based on the container configuration.
 */
@BlockMeta(id = "setTime", displayName = "§aSet Time", type = BlockType.ACTION)
public class SetTimeAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get time parameters from the container configuration
            SetTimeParams params = getTimeParamsFromContainer(block, context);

            // Set the time in the world
            World world = player.getWorld();
            if (params.relative) {
                world.setTime(world.getTime() + params.time);
            } else {
                world.setTime(params.time);
            }

            return ExecutionResult.success("World time set to " + params.time);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set world time: " + e.getMessage());
        }
    }

    /**
     * Gets time parameters from the container configuration
     */
    private SetTimeParams getTimeParamsFromContainer(CodeBlock block, ExecutionContext context) {
        SetTimeParams params = new SetTimeParams();

        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();

            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());

            if (slotResolver != null) {
                // Get time from the time slot
                Integer timeSlot = slotResolver.apply("time_slot");
                if (timeSlot != null) {
                    ItemStack timeItem = block.getConfigItem(timeSlot);
                    if (timeItem != null && timeItem.hasItemMeta()) {
                        // Extract time from item
                        params.time = getTimeFromItem(timeItem);
                    }
                }

                // Get relative from the relative slot
                Integer relativeSlot = slotResolver.apply("relative_slot");
                if (relativeSlot != null) {
                    ItemStack relativeItem = block.getConfigItem(relativeSlot);
                    if (relativeItem != null && relativeItem.hasItemMeta()) {
                        // Extract relative from item
                        params.relative = getBooleanFromItem(relativeItem, false);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting time parameters from container in SetTimeAction: " + e.getMessage());
        }

        return params;
    }

    /**
     * Extracts time from an item
     */
    private long getTimeFromItem(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse time from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Long.parseLong(cleanName);
                }
            }

            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return 0; // Default to 0
        }
    }

    /**
     * Extracts boolean from an item
     */
    private boolean getBooleanFromItem(ItemStack item, boolean defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse boolean from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim().toLowerCase();
                    return "true".equals(cleanName) || "1".equals(cleanName) || "yes".equals(cleanName);
                }
            }

            // Fallback: odd = true, even = false
            return item.getAmount() % 2 == 1;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Helper class to hold time parameters
     */
    private static class SetTimeParams {
        long time = 0;
        boolean relative = false;
    }
}
