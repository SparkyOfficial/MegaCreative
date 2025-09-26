package com.megacreative.coding.actions.world;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for creating an explosion.
 * This action creates an explosion at the player's location from container configuration.
 */
@BlockMeta(id = "explosion", displayName = "§aCreate Explosion", type = BlockType.ACTION)
public class ExplosionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get explosion parameters from the container configuration
            ExplosionParams params = getExplosionParamsFromContainer(block, context);

            // Create the explosion at the player's location
            Location location = player.getLocation();
            boolean success = player.getWorld().createExplosion(location, params.power, params.fire, params.breakBlocks);

            if (success) {
                return ExecutionResult.success("Created explosion with power " + params.power);
            } else {
                return ExecutionResult.error("Failed to create explosion");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create explosion: " + e.getMessage());
        }
    }

    /**
     * Gets explosion parameters from the container configuration
     */
    private ExplosionParams getExplosionParamsFromContainer(CodeBlock block, ExecutionContext context) {
        ExplosionParams params = new ExplosionParams();

        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();

            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());

            if (slotResolver != null) {
                // Get power from the power slot
                Integer powerSlot = slotResolver.apply("power_slot");
                if (powerSlot != null) {
                    ItemStack powerItem = block.getConfigItem(powerSlot);
                    if (powerItem != null && powerItem.hasItemMeta()) {
                        params.power = getFloatFromItem(powerItem, 4.0f);
                    }
                }

                // Get fire from the fire slot
                Integer fireSlot = slotResolver.apply("fire_slot");
                if (fireSlot != null) {
                    ItemStack fireItem = block.getConfigItem(fireSlot);
                    if (fireItem != null && fireItem.hasItemMeta()) {
                        params.fire = getBooleanFromItem(fireItem, false);
                    }
                }

                // Get breakBlocks from the break_blocks slot
                Integer breakBlocksSlot = slotResolver.apply("break_blocks_slot");
                if (breakBlocksSlot != null) {
                    ItemStack breakBlocksItem = block.getConfigItem(breakBlocksSlot);
                    if (breakBlocksItem != null && breakBlocksItem.hasItemMeta()) {
                        params.breakBlocks = getBooleanFromItem(breakBlocksItem, true);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning(
                    "Error getting explosion parameters from container in ExplosionAction: " + e.getMessage()
            );
        }

        return params;
    }

    /**
     * Extracts float from an item
     */
    private float getFloatFromItem(ItemStack item, float defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Math.max(0, Float.parseFloat(cleanName));
                }
            }
            return Math.max(0, item.getAmount());
        } catch (Exception e) {
            return Math.max(0, defaultValue);
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
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim().toLowerCase();
                    return "true".equals(cleanName)
                            || "1".equals(cleanName)
                            || "yes".equals(cleanName);
                }
            }
            return item.getAmount() % 2 == 1;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Helper class to hold explosion parameters
     */
    private static class ExplosionParams {
        float power = 4.0f;
        boolean fire = false;
        boolean breakBlocks = true;
    }
}
