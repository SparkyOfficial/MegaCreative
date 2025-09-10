package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.Collection;

/**
 * Condition for checking if a specific entity is near the player.
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
            // Get the entity type parameter from the block
            DataValue entityValue = block.getParameter("entity");
            if (entityValue == null) {
                return false;
            }

            // Get optional distance parameter (default to 5)
            double distance = 5.0;
            DataValue distanceValue = block.getParameter("distance");
            if (distanceValue != null) {
                try {
                    distance = Math.max(1, distanceValue.asNumber().doubleValue());
                } catch (NumberFormatException e) {
                    // Use default distance if parsing fails
                }
            }

            // Resolve any placeholders in the entity type
            ParameterResolver resolver = new ParameterResolver(context);
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
}