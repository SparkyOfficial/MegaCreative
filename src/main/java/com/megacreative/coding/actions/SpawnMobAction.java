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
 * This action retrieves mob parameters from the new parameter system and spawns the mob.
 */
public class SpawnMobAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue mobValue = block.getParameter("mob");
            DataValue countValue = block.getParameter("count");
            
            if (mobValue == null || mobValue.isEmpty()) {
                return ExecutionResult.error("No mob type provided");
            }

            
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(mobValue.asString().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid mob type: " + mobValue.asString());
            }

            int count = 1;
            if (countValue != null && !countValue.isEmpty()) {
                try {
                    count = Math.max(1, Integer.parseInt(countValue.asString()));
                } catch (NumberFormatException e) {
                    
                }
            }

            
            Location location = player.getLocation();

            
            for (int i = 0; i < count; i++) {
                player.getWorld().spawnEntity(location, entityType);
            }
            
            return ExecutionResult.success("Spawned " + count + " " + entityType.name() + "(s) successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to spawn mob: " + e.getMessage());
        }
    }
}