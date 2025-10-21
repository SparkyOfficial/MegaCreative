package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a player is riding a specific entity from the new parameter system.
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
            
            DataValue entityValue = block.getParameter("entity");
            
            
            if (entityValue != null && !entityValue.isEmpty()) {
                
                ParameterResolver resolver = new ParameterResolver(context);
                DataValue resolvedEntity = resolver.resolve(context, entityValue);
                
                
                String entityName = resolvedEntity.asString();
                // Fix for Qodana issue: Condition entityName == null is always false
                // This was a false positive - we need to properly check for empty strings
                if (entityName.isEmpty()) {
                    context.getPlugin().getLogger().warning("IsRidingCondition: 'entity' parameter is empty.");
                    return false;
                }
                
                try {
                    EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                    if (player.isInsideVehicle()) {
                        org.bukkit.entity.Entity vehicle = player.getVehicle();
                        if (vehicle != null) {
                            return vehicle.getType() == entityType;
                        }
                    }
                    return false;
                } catch (IllegalArgumentException e) {
                    context.getPlugin().getLogger().warning("IsRidingCondition: Invalid entity type '" + entityName + "'.");
                    return false;
                }
            } else {
                
                return player.isInsideVehicle();
            }
        } catch (Exception e) {
            
            context.getPlugin().getLogger().warning("Error in IsRidingCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold entity parameters
     */
    private static class IsRidingParams {
        String entityStr = "";
    }
}