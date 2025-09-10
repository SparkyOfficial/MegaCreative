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
 * This action changes the world time based on the parameter.
 */
public class SetTimeAction implements BlockAction {

    @Override
public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the time parameter from the block
            DataValue timeValue = block.getParameter("time");
            if (timeValue == null) {
                return ExecutionResult.error("Time parameter is missing");
            }

            // Resolve any placeholders in the time
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTime = resolver.resolve(context, timeValue);
            
            // Parse time parameter
            long time;
            try {
                time = resolvedTime.asNumber().longValue();
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid time value: " + resolvedTime.asString());
            }

            // Get optional relative parameter (default to false)
            boolean relative = false;
            DataValue relativeValue = block.getParameter("relative");
            if (relativeValue != null) {
                DataValue resolvedRelative = resolver.resolve(context, relativeValue);
                relative = resolvedRelative.asBoolean();
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