package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.Collection;

/**
 * Condition for checking if a specific entity is near the player from the new parameter system.
 * This condition returns true if the specified entity type is within a specified distance of the player.
 */
@BlockMeta(id = "isNearEntity", displayName = "§aIs Near Entity", type = BlockType.CONDITION)
public class IsNearEntityCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            
            DataValue entityValue = block.getParameter("entity");
            DataValue distanceValue = block.getParameter("distance");
            
            if (entityValue == null || entityValue.isEmpty()) {
                context.getPlugin().getLogger().warning("IsNearEntityCondition: 'entity' parameter is missing.");
                return false;
            }

            
            double distance = 5.0;
            if (distanceValue != null && !distanceValue.isEmpty()) {
                try {
                    distance = Math.max(1, distanceValue.asNumber().doubleValue());
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("IsNearEntityCondition: Invalid distance value, using default 5.");
                }
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedEntity = resolver.resolve(context, entityValue);
            
            
            String entityName = resolvedEntity.asString();
            if (entityName == null || entityName.isEmpty()) {
                context.getPlugin().getLogger().warning("IsNearEntityCondition: 'entity' parameter is empty.");
                return false;
            }

            
            try {
                EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                Location playerLocation = player.getLocation();
                
                
                Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(playerLocation, distance, distance, distance);
                
                
                for (Entity entity : nearbyEntities) {
                    if (entity.getType() == entityType) {
                        return true;
                    }
                }
                
                return false;
            } catch (IllegalArgumentException e) {
                context.getPlugin().getLogger().warning("IsNearEntityCondition: Invalid entity type '" + entityName + "'.");
                return false;
            }
        } catch (Exception e) {
            
            context.getPlugin().getLogger().warning("Error in IsNearEntityCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold entity parameters
     */
    private static class IsNearEntityParams {
        String entityStr = "";
        String distanceStr = "";
    }
}