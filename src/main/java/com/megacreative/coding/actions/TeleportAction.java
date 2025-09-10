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
 * Action for teleporting a player to a location.
 * This action retrieves location coordinates from the block parameters and teleports the player.
 */
public class TeleportAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the location parameter from the block
            DataValue locationValue = block.getParameter("location");
            if (locationValue == null) {
                return ExecutionResult.error("Location parameter is missing");
            }

            // Resolve any placeholders in the location
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedLocation = resolver.resolve(context, locationValue);
            
            // Check if the resolved value is a LocationValue
            if (resolvedLocation instanceof com.megacreative.coding.values.LocationValue) {
                Location location = (Location) resolvedLocation.getValue();
                if (location != null) {
                    player.teleport(location);
                    return ExecutionResult.success("Player teleported successfully");
                } else {
                    return ExecutionResult.error("Location is null");
                }
            } else {
                return ExecutionResult.error("Location parameter is not a valid LocationValue");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to teleport player: " + e.getMessage());
        }
    }
}