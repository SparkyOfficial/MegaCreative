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
 * Action to spawn a mob
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "spawnMob", displayName = "§bSpawn Mob", type = BlockType.ACTION)
public class SpawnMobAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue mobValue = block.getParameter("mob");
            DataValue countValue = block.getParameter("count");
            DataValue radiusValue = block.getParameter("radius");
            
            if (mobValue == null) {
                return ExecutionResult.error("Missing required parameter: mob");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMob = resolver.resolve(context, mobValue);
            
            String mobStr = resolvedMob.asString();
            EntityType mobType;
            
            try {
                mobType = EntityType.valueOf(mobStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid mob type: " + mobStr);
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
            
            // Spawn mobs
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
                
                // Spawn mob
                if (player.getWorld().spawnEntity(spawnLocation, mobType) != null) {
                    spawned++;
                }
            }
            
            return ExecutionResult.success("Spawned " + spawned + " " + mobType.name() + "(s)");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to spawn mob: " + e.getMessage());
        }
    }
}