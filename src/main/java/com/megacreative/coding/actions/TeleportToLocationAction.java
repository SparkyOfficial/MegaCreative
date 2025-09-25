package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Action for teleporting a player to a specific location.
 * This action retrieves location coordinates and teleports the player.
 */
@BlockMeta(id = "teleportToLocation", displayName = "§aTeleport to Location", type = BlockType.ACTION)
public class TeleportToLocationAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get location parameters
            com.megacreative.coding.values.DataValue xValue = block.getParameter("x");
            com.megacreative.coding.values.DataValue yValue = block.getParameter("y");
            com.megacreative.coding.values.DataValue zValue = block.getParameter("z");
            com.megacreative.coding.values.DataValue worldValue = block.getParameter("world");
            
            if (xValue == null || yValue == null || zValue == null) {
                return ExecutionResult.error("Location coordinates are not configured");
            }

            double x = Double.parseDouble(xValue.asString());
            double y = Double.parseDouble(yValue.asString());
            double z = Double.parseDouble(zValue.asString());
            
            World world = player.getWorld(); // Default to current world
            if (worldValue != null && !worldValue.isEmpty()) {
                World targetWorld = org.bukkit.Bukkit.getWorld(worldValue.asString());
                if (targetWorld != null) {
                    world = targetWorld;
                }
            }
            
            Location location = new Location(world, x, y, z);

            // Teleport the player
            player.teleport(location);
            return ExecutionResult.success("Player teleported successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to teleport player: " + e.getMessage());
        }
    }
}
