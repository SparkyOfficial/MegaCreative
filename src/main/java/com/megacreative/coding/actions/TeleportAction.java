package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Action for teleporting a player to a location.
 * This action retrieves location coordinates from the new parameter system and teleports the player.
 */
@BlockMeta(id = "teleport", displayName = "§aTeleport", type = BlockType.ACTION)
public class TeleportAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get location parameters from the new parameter system
            DataValue xValue = block.getParameter("x");
            DataValue yValue = block.getParameter("y");
            DataValue zValue = block.getParameter("z");
            DataValue worldValue = block.getParameter("world");
            
            if (xValue == null || yValue == null || zValue == null) {
                return ExecutionResult.error("Location coordinates are not configured");
            }

            // Parse coordinates
            double x = Double.parseDouble(xValue.asString());
            double y = Double.parseDouble(yValue.asString());
            double z = Double.parseDouble(zValue.asString());
            
            // Determine world (default to current world)
            World world = player.getWorld();
            if (worldValue != null && !worldValue.isEmpty()) {
                World targetWorld = org.bukkit.Bukkit.getWorld(worldValue.asString());
                if (targetWorld != null) {
                    world = targetWorld;
                }
            }

            // Create location and teleport player
            Location location = new Location(world, x, y, z);
            player.teleport(location);
            
            return ExecutionResult.success("Player teleported successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to teleport player: " + e.getMessage());
        }
    }
}