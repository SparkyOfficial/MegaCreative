package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Action to spawn an entity
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "spawnEntity", displayName = "§bSpawn Entity", type = BlockType.ACTION)
public class SpawnEntityAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue entityValue = block.getParameter("entity");
            DataValue countValue = block.getParameter("count");
            DataValue radiusValue = block.getParameter("radius");
            
            if (entityValue == null) {
                return ExecutionResult.error("Missing required parameter: entity");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedEntity = resolver.resolve(context, entityValue);
            
            String entityStr = resolvedEntity.asString();
            EntityType entityType;
            
            try {
                entityType = EntityType.valueOf(entityStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid entity type: " + entityStr);
            }
            
            // Get count (default to 1)
            int count = 1;
            if (countValue != null) {
                DataValue resolvedCount = resolver.resolve(context, countValue);
                count = Math.max(1, resolvedCount.asNumber().intValue());
            }
            
            // Get radius (default to 0 - spawn at player location)
            double radius = 0.0;
            if (radiusValue != null) {
                DataValue resolvedRadius = resolver.resolve(context, radiusValue);
                radius = Math.max(0.0, resolvedRadius.asNumber().doubleValue());
            }
            
            // Spawn entities
            Location baseLocation = player.getLocation();
            int spawned = 0;
            
            for (int i = 0; i < count; i++) {
                Location spawnLocation = baseLocation.clone();
                
                // Add random offset if radius > 0
                if (radius > 0) {
                    double offsetX = (Math.random() * 2 - 1) * radius;
                    double offsetY = (Math.random() * 2 - 1) * radius;
                    double offsetZ = (Math.random() * 2 - 1) * radius;
                    spawnLocation.add(offsetX, offsetY, offsetZ);
                }
                
                // Spawn entity
                if (player.getWorld().spawnEntity(spawnLocation, entityType) != null) {
                    spawned++;
                }
            }
            
            return ExecutionResult.success("Spawned " + spawned + " " + entityType.name() + "(s)");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to spawn entity: " + e.getMessage());
        }
    }
}