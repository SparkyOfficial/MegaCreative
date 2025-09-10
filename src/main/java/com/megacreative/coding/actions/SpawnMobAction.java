package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for spawning a mob at a location.
 * This action retrieves mob parameters from the container configuration and spawns the mob.
 */
public class SpawnMobAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get mob parameters from the container configuration
            SpawnMobParams params = getMobParamsFromContainer(block, context);
            
            if (params.entityType == null) {
                return ExecutionResult.error("Mob type is not configured");
            }

            // Get the location where the mob should be spawned (default to player location)
            Location location = player.getLocation();

            // Spawn the mob
            for (int i = 0; i < params.count; i++) {
                player.getWorld().spawnEntity(location, params.entityType);
            }
            
            return ExecutionResult.success("Spawned " + params.count + " " + params.entityType.name() + "(s) successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to spawn mob: " + e.getMessage());
        }
    }
    
    /**
     * Gets mob parameters from the container configuration
     */
    private SpawnMobParams getMobParamsFromContainer(CodeBlock block, ExecutionContext context) {
        SpawnMobParams params = new SpawnMobParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get mob from the mob slot
                Integer mobSlot = slotResolver.apply("mob_slot");
                if (mobSlot != null) {
                    ItemStack mobItem = block.getConfigItem(mobSlot);
                    if (mobItem != null && mobItem.hasItemMeta()) {
                        // Extract mob type from item
                        params.entityTypeName = getMobNameFromItem(mobItem);
                        if (params.entityTypeName != null) {
                            try {
                                params.entityType = EntityType.valueOf(params.entityTypeName.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                // Use default mob if parsing fails
                                params.entityType = EntityType.ZOMBIE;
                            }
                        }
                    }
                }
                
                // Get count from the count slot
                Integer countSlot = slotResolver.apply("count_slot");
                if (countSlot != null) {
                    ItemStack countItem = block.getConfigItem(countSlot);
                    if (countItem != null && countItem.hasItemMeta()) {
                        // Extract count from item
                        params.count = getCountFromItem(countItem, 1);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting mob parameters from container in SpawnMobAction: " + e.getMessage());
        }
        
        // Set defaults if not configured
        if (params.entityType == null) {
            params.entityType = EntityType.ZOMBIE;
        }
        
        return params;
    }
    
    /**
     * Extracts mob name from an item
     */
    private String getMobNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the mob name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts count from an item
     */
    private int getCountFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse count from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Math.max(1, Integer.parseInt(cleanName));
                }
            }
            
            // Fallback to item amount
            return Math.max(1, item.getAmount());
        } catch (Exception e) {
            return Math.max(1, defaultValue);
        }
    }
    
    /**
     * Helper class to hold mob parameters
     */
    private static class SpawnMobParams {
        EntityType entityType = null;
        String entityTypeName = "";
        int count = 1;
    }
}