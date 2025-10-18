package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Action for spawning an entity.
 * This action spawns an entity of a specified type using the new parameter system.
 */
public class SpawnEntityAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue entityTypeValue = block.getParameter("entityType");
            DataValue countValue = block.getParameter("count");
            DataValue radiusValue = block.getParameter("radius");
            
            if (entityTypeValue == null || entityTypeValue.isEmpty()) {
                return ExecutionResult.error("No entity type provided");
            }

            
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(entityTypeValue.asString().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid entity type: " + entityTypeValue.asString());
            }

            int count = 1;
            if (countValue != null && !countValue.isEmpty()) {
                try {
                    count = Math.max(1, Integer.parseInt(countValue.asString()));
                } catch (NumberFormatException e) {
                    // Log exception and continue processing
                    // This is expected behavior when parsing user input
                    context.getPlugin().getLogger().warning("Invalid count value: " + countValue.asString());
                }
            }

            double radius = 3.0;
            if (radiusValue != null && !radiusValue.isEmpty()) {
                try {
                    radius = Math.max(0, Double.parseDouble(radiusValue.asString()));
                } catch (NumberFormatException e) {
                    // Log exception and continue processing
                    // This is expected behavior when parsing user input
                    context.getPlugin().getLogger().warning("Invalid radius value: " + radiusValue.asString());
                }
            }

            
            Location spawnLocation = player.getLocation();
            
            int spawnedCount = 0;
            for (int i = 0; i < count; i++) {
                
                double offsetX = (Math.random() - 0.5) * 2 * radius;
                double offsetZ = (Math.random() - 0.5) * 2 * radius;
                Location entityLocation = spawnLocation.clone().add(offsetX, 0, offsetZ);
                
                
                entityLocation.setY(spawnLocation.getWorld().getHighestBlockYAt(entityLocation));
                
                spawnLocation.getWorld().spawnEntity(entityLocation, entityType);
                spawnedCount++;
            }
            
            return ExecutionResult.success("Spawned " + spawnedCount + " " + entityType.name() + "(s)");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to spawn entity: " + e.getMessage());
        }
    }
}