package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

/**
 * Condition for checking if a player is riding a specific entity from container configuration.
 * This condition returns true if the player is riding the specified entity type.
 */
@BlockMeta(id = "isRiding", displayName = "§aIs Riding", type = BlockType.CONDITION)
public class IsRidingCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            IsRidingParams params = getEntityParamsFromContainer(block, context);
            
            // If a specific entity type is provided, check for that type
            if (params.entityStr != null && !params.entityStr.isEmpty()) {
                // Resolve any placeholders in the entity name
                String resolvedEntityStr = params.entityStr;
                
                // Parse entity type parameter
                if (resolvedEntityStr == null || resolvedEntityStr.isEmpty()) {
                    return false;
                }
                
                try {
                    EntityType entityType = EntityType.valueOf(resolvedEntityStr.toUpperCase());
                    if (player.isInsideVehicle()) {
                        org.bukkit.entity.Entity vehicle = player.getVehicle();
                        if (vehicle != null) {
                            return vehicle.getType() == entityType;
                        }
                    }
                    return false;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            } else {
                // If no entity type is specified, just check if the player is riding anything
                return player.isInsideVehicle();
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets entity parameters from the container configuration
     */
    private IsRidingParams getEntityParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IsRidingParams params = new IsRidingParams();
        
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
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting entity parameters from container in IsRidingCondition: " + e.getMessage());
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
     * Helper class to hold entity parameters
     */
    private static class IsRidingParams {
        String entityStr = "";
    }
}