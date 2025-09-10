package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Action for spawning an entity.
 * This action spawns an entity of a specified type.
 */
public class SpawnEntityAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the entity type parameter from the block
            DataValue entityTypeValue = block.getParameter("entityType");
            if (entityTypeValue == null) {
                return ExecutionResult.error("Entity type parameter is missing");
            }

            // Get the count parameter from the block (default to 1)
            int count = 1;
            DataValue countValue = block.getParameter("count");
            if (countValue != null) {
                try {
                    count = Math.max(1, countValue.asNumber().intValue());
                } catch (NumberFormatException e) {
                    // Use default count if parsing fails
                }
            }

            // Get the radius parameter from the block (default to 3)
            double radius = 3.0;
            DataValue radiusValue = block.getParameter("radius");
            if (radiusValue != null) {
                try {
                    radius = Math.max(0, radiusValue.asNumber().doubleValue());
                } catch (NumberFormatException e) {
                    // Use default radius if parsing fails
                }
            }

            // Resolve any placeholders in the entity type
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedEntityType = resolver.resolve(context, entityTypeValue);
            
            // Parse entity type parameter
            String entityTypeName = resolvedEntityType.asString();
            if (entityTypeName == null || entityTypeName.isEmpty()) {
                return ExecutionResult.error("Entity type is empty or null");
            }

            // Spawn the entities
            try {
                EntityType entityType = EntityType.valueOf(entityTypeName.toUpperCase());
                Location spawnLocation = player.getLocation();
                
                int spawnedCount = 0;
                for (int i = 0; i < count; i++) {
                    // Add some randomness to the spawn location within the radius
                    double offsetX = (Math.random() - 0.5) * 2 * radius;
                    double offsetZ = (Math.random() - 0.5) * 2 * radius;
                    Location entityLocation = spawnLocation.clone().add(offsetX, 0, offsetZ);
                    
                    // Make sure the entity spawns on the ground
                    entityLocation.setY(spawnLocation.getWorld().getHighestBlockYAt(entityLocation));
                    
                    if (spawnLocation.getWorld().spawnEntity(entityLocation, entityType) != null) {
                        spawnedCount++;
                    }
                }
                
                return ExecutionResult.success("Spawned " + spawnedCount + " " + entityType.name() + "(s)");
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid entity type: " + entityTypeName);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to spawn entity: " + e.getMessage());
        }
    }
}