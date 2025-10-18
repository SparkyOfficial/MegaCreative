package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Action for creating an explosion.
 * This action creates an explosion at the player's location using the new parameter system.
 */
@BlockMeta(id = "explosion", displayName = "§aCreate Explosion", type = BlockType.ACTION)
public class ExplosionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue powerValue = block.getParameter("power");
            DataValue fireValue = block.getParameter("fire");
            DataValue breakBlocksValue = block.getParameter("breakBlocks");

            
            float power = 4.0f;
            if (powerValue != null && !powerValue.isEmpty()) {
                try {
                    power = Math.max(0, Float.parseFloat(powerValue.asString()));
                } catch (NumberFormatException e) {
                    // Log exception and continue processing
                    // This is expected behavior when parsing user input
                    // Use default power when parsing fails
                }
            }

            boolean fire = false;
            if (fireValue != null && !fireValue.isEmpty()) {
                fire = Boolean.parseBoolean(fireValue.asString());
            }

            boolean breakBlocks = true;
            if (breakBlocksValue != null && !breakBlocksValue.isEmpty()) {
                breakBlocks = Boolean.parseBoolean(breakBlocksValue.asString());
            }

            
            Location location = player.getLocation();
            boolean success = player.getWorld().createExplosion(location, power, fire, breakBlocks);
            
            if (success) {
                return ExecutionResult.success("Created explosion with power " + power);
            } else {
                return ExecutionResult.error("Failed to create explosion");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create explosion: " + e.getMessage());
        }
    }
}