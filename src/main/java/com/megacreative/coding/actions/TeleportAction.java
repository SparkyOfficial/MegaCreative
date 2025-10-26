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
import org.bukkit.entity.Player;

/**
 * Action to teleport a player to a specific location
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "teleport", displayName = "§bTeleport Player", type = BlockType.ACTION)
public class TeleportAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue xValue = block.getParameter("x");
            DataValue yValue = block.getParameter("y");
            DataValue zValue = block.getParameter("z");
            DataValue worldValue = block.getParameter("world");
            DataValue yawValue = block.getParameter("yaw");
            DataValue pitchValue = block.getParameter("pitch");
            
            if (xValue == null || yValue == null || zValue == null) {
                return ExecutionResult.error("Missing required coordinates (x, y, z)");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedX = resolver.resolve(context, xValue);
            DataValue resolvedY = resolver.resolve(context, yValue);
            DataValue resolvedZ = resolver.resolve(context, zValue);
            
            double x = resolvedX.asNumber().doubleValue();
            double y = resolvedY.asNumber().doubleValue();
            double z = resolvedZ.asNumber().doubleValue();
            
            // Get world (default to player's current world)
            String worldName = player.getWorld().getName();
            if (worldValue != null) {
                DataValue resolvedWorld = resolver.resolve(context, worldValue);
                worldName = resolvedWorld.asString();
            }
            
            // Get rotation (default to player's current rotation)
            float yaw = player.getLocation().getYaw();
            float pitch = player.getLocation().getPitch();
            
            if (yawValue != null) {
                DataValue resolvedYaw = resolver.resolve(context, yawValue);
                yaw = resolvedYaw.asNumber().floatValue();
            }
            
            if (pitchValue != null) {
                DataValue resolvedPitch = resolver.resolve(context, pitchValue);
                pitch = resolvedPitch.asNumber().floatValue();
            }
            
            // Create location
            Location location = new Location(
                player.getWorld(), // We'll use the player's world for now
                x, y, z,
                yaw, pitch
            );
            
            // Teleport player
            player.teleport(location);
            
            return ExecutionResult.success("Player teleported to " + x + ", " + y + ", " + z);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to teleport player: " + e.getMessage());
        }
    }
}