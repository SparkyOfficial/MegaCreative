package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.function.Function;

/**
 * Condition for checking if a specific entity is near the player from container configuration.
 * This condition returns true if the specified entity type is within a specified distance of the player.
 */
public class IsNearEntityCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            IsNearEntityParams params = getEntityParamsFromContainer(block, context);
            
            if (params.entityStr == null || params.entityStr.isEmpty()) {
                return false;
            }

            // Parse distance parameter (default to 5)
            double distance = 5.0;
            if (params.distanceStr != null && !params.distanceStr.isEmpty()) {
                try {
                    distance = Math.max(1, Double.parseDouble(params.distanceStr));
                } catch (NumberFormatException e) {
                    // Use default distance if parsing fails
                }
            }

            // Resolve any placeholders in the entity type
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue entityValue = DataValue.of(params.entityStr);
            DataValue resolvedEntity = resolver.resolve(context, entityValue);
            
            // Parse entity type parameter
            String entityName = resolvedEntity.asString();
            if (entityName == null || entityName.isEmpty()) {
                return false;
            }

            // Check if the specified entity type is near the player
            try {
                EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                Location playerLocation = player.getLocation();
                
                // Get nearby entities within the specified distance
                Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(playerLocation, distance, distance, distance);
                
                // Check if any of the nearby entities are of the specified type
                for (Entity entity : nearbyEntities) {
                    if (entity.getType() == entityType) {
                        return true;
                    }
                }
                
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets entity parameters from the container configuration
     */
    private IsNearEntityParams getEntityParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IsNearEntityParams params = new IsNearEntityParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get entity from the entity_slot
                Integer entitySlot = slotResolver.apply("entity_slot");
                if (entitySlot != null) {
                    ItemStack entityItem = block.getConfigItem(entitySlot);
                    if (entityItem != null) {
                        // Extract entity type from item
                        params.entityStr = getEntityTypeFromItem(entityItem);
                    }
                }
                
                // Get distance from the radius_slot
                Integer radiusSlot = slotResolver.apply("radius_slot");
                if (radiusSlot != null) {
                    ItemStack radiusItem = block.getConfigItem(radiusSlot);
                    if (radiusItem != null && radiusItem.hasItemMeta()) {
                        // Extract distance from item
                        params.distanceStr = getDistanceFromItem(radiusItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting entity parameters from container in IsNearEntityCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts entity type from an item
     */
    private String getEntityTypeFromItem(ItemStack item) {
        // For entity type, we'll use the item type name
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
     * Helper class to hold entity parameters
     */
    private static class IsNearEntityParams {
        String entityStr = "";
        String distanceStr = "";
    }
}