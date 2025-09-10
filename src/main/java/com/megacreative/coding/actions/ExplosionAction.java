package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Action for creating an explosion.
 * This action creates an explosion at the player's location.
 */
public class ExplosionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the power parameter from the block (default to 4.0)
            float power = 4.0f;
            DataValue powerValue = block.getParameter("power");
            if (powerValue != null) {
                try {
                    power = Math.max(0, powerValue.asNumber().floatValue());
                } catch (NumberFormatException e) {
                    // Use default power if parsing fails
                }
            }

            // Get the fire parameter from the block (default to false)
            boolean fire = false;
            DataValue fireValue = block.getParameter("fire");
            if (fireValue != null) {
                fire = "true".equalsIgnoreCase(fireValue.asString()) || "1".equals(fireValue.asString());
            }

            // Get the breakBlocks parameter from the block (default to true)
            boolean breakBlocks = true;
            DataValue breakBlocksValue = block.getParameter("breakBlocks");
            if (breakBlocksValue != null) {
                breakBlocks = !"false".equalsIgnoreCase(breakBlocksValue.asString()) && !"0".equals(breakBlocksValue.asString());
            }

            // Create the explosion at the player's location
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