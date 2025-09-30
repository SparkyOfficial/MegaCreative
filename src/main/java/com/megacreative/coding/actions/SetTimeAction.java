package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.World;

/**
 * Action for setting the time in a world.
 * This action changes the world time based on the new parameter system.
 */
public class SetTimeAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get time parameters from the new parameter system
            DataValue timeValue = block.getParameter("time");
            DataValue relativeValue = block.getParameter("relative");

            // Parse parameters with defaults
            long time = 0;
            if (timeValue != null && !timeValue.isEmpty()) {
                try {
                    time = Long.parseLong(timeValue.asString());
                } catch (NumberFormatException e) {
                    // Use default time
                }
            }

            boolean relative = false;
            if (relativeValue != null && !relativeValue.isEmpty()) {
                relative = Boolean.parseBoolean(relativeValue.asString());
            }

            // Set the time in the world
            World world = player.getWorld();
            if (relative) {
                world.setTime(world.getTime() + time);
            } else {
                world.setTime(time);
            }
            
            return ExecutionResult.success("World time set to " + time);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set world time: " + e.getMessage());
        }
    }
}