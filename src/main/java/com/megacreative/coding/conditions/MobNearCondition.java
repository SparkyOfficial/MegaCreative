package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a mob is near the player from container configuration.
 * This condition returns true if a mob is within a specified distance of the player.
 */
public class MobNearCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            MobNearParams params = getMobParamsFromContainer(block, context);
            
            // Parse distance parameter (default to 10)
            int distance = 10;
            if (params.distanceStr != null && !params.distanceStr.isEmpty()) {
                try {
                    distance = Math.max(1, Integer.parseInt(params.distanceStr));
                } catch (NumberFormatException e) {
                    // Use default distance if parsing fails
                }
            }

            // Check for mobs near the player
            for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
                if (entity instanceof LivingEntity) {
                    // If a specific mob type is specified, check if it matches
                    if (params.mobStr != null && !params.mobStr.isEmpty()) {
                        try {
                            EntityType mobType = EntityType.valueOf(params.mobStr.toUpperCase());
                            if (entity.getType() == mobType) {
                                return true;
                            }
                        } catch (IllegalArgumentException e) {
                            // If mob type is invalid, continue checking other entities
                        }
                    } else {
                        // If no specific mob type is specified, any mob will do
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets mob parameters from the container configuration
     */
    private MobNearParams getMobParamsFromContainer(CodeBlock block, ExecutionContext context) {
        MobNearParams params = new MobNearParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get mob from the mob_slot
                Integer mobSlot = slotResolver.apply("mob_slot");
                if (mobSlot != null) {
                    ItemStack mobItem = block.getConfigItem(mobSlot);
                    if (mobItem != null) {
                        // Extract mob type from item
                        params.mobStr = getMobTypeFromItem(mobItem);
                    }
                }
                
                // Get distance from the distance_slot
                Integer distanceSlot = slotResolver.apply("distance_slot");
                if (distanceSlot != null) {
                    ItemStack distanceItem = block.getConfigItem(distanceSlot);
                    if (distanceItem != null && distanceItem.hasItemMeta()) {
                        // Extract distance from item
                        params.distanceStr = getDistanceFromItem(distanceItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting mob parameters from container in MobNearCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts mob type from an item
     */
    private String getMobTypeFromItem(ItemStack item) {
        // For mob type, we'll use the item type name
        return item.getType().name();
    }
    
    /**
     * Extracts distance from an item
     */
    private String getDistanceFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the distance
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Helper class to hold mob parameters
     */
    private static class MobNearParams {
        String mobStr = "";
        String distanceStr = "";
    }
}