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
 * Action for spawning a mob at a location.
 * This action retrieves mob parameters from the block and spawns the mob.
 */
public class SpawnMobAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the mob type parameter from the block
            DataValue mobValue = block.getParameter("mob");
            if (mobValue == null) {
                return ExecutionResult.error("Mob parameter is missing");
            }

            // Resolve any placeholders in the mob type
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMob = resolver.resolve(context, mobValue);
            
            // Parse mob parameters
            String mobName = resolvedMob.asString();
            if (mobName == null || mobName.isEmpty()) {
                return ExecutionResult.error("Mob name is empty or null");
            }

            // Get the location where the mob should be spawned (default to player location)
            Location location = player.getLocation();
            
            // Get optional count parameter
            int count = 1;
            DataValue countValue = block.getParameter("count");
            if (countValue != null) {
                DataValue resolvedCount = resolver.resolve(context, countValue);
                try {
                    count = Math.max(1, resolvedCount.asNumber().intValue());
                } catch (NumberFormatException e) {
                    // Use default count if parsing fails
                }
            }

            // Spawn the mob
            try {
                EntityType entityType = EntityType.valueOf(mobName.toUpperCase());
                
                for (int i = 0; i < count; i++) {
                    player.getWorld().spawnEntity(location, entityType);
                }
                
                return ExecutionResult.success("Spawned " + count + " " + mobName + "(s) successfully");
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid mob type: " + mobName);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to spawn mob: " + e.getMessage());
        }
    }
}