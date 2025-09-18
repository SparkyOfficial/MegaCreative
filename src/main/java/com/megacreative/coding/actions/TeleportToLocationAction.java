package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.World;

/**
 * Enhanced Teleport action that allows teleporting to a specific location with effects
 * 
 * Parameters:
 * - "location": The target location in format "world:x,y,z" or "x,y,z" (uses player's world)
 * - "effects": Whether to play teleport effects (true/false, default: true)
 * - "message": Custom message to send to player after teleport (optional)
 */
public class TeleportToLocationAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (player == null) {
            return ExecutionResult.error("No player available for teleportation");
        }
        
        try {
            // Get location parameter
            DataValue locationValue = block.getParameter("location");
            if (locationValue == null || locationValue.isEmpty()) {
                return ExecutionResult.error("Location parameter is required for teleportation");
            }
            
            String locationString = locationValue.asString();
            Location targetLocation = parseLocationString(locationString, context);
            
            if (targetLocation == null) {
                return ExecutionResult.error("Invalid location format: " + locationString);
            }
            
            // Get effects parameter (default to true)
            DataValue effectsValue = block.getParameter("effects");
            boolean playEffects = effectsValue == null || effectsValue.asBoolean();
            
            // Get message parameter (optional)
            DataValue messageValue = block.getParameter("message");
            String customMessage = messageValue != null ? messageValue.asString() : null;
            
            // Play departure effects
            if (playEffects) {
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 1.0, 0.5, 0.1);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
            
            // Teleport player
            player.teleport(targetLocation);
            
            // Play arrival effects
            if (playEffects) {
                player.getWorld().spawnParticle(Particle.PORTAL, targetLocation, 50, 0.5, 1.0, 0.5, 0.1);
                player.playSound(targetLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
            
            // Send custom message or default message
            if (customMessage != null && !customMessage.isEmpty()) {
                player.sendMessage(customMessage);
            } else {
                player.sendMessage("§aTeleported to coordinates: " + targetLocation.getBlockX() + ", " + 
                                 targetLocation.getBlockY() + ", " + targetLocation.getBlockZ());
            }
            
            return ExecutionResult.success("Player teleported successfully");
            
        } catch (Exception e) {
            return ExecutionResult.error("Error during teleportation: " + e.getMessage());
        }
    }
    
    /**
     * Parse a location string in the format "world:x,y,z" or "x,y,z" (uses player's world)
     * @param locString The location string to parse
     * @param context The execution context
     * @return The parsed Location or null if invalid
     */
    private Location parseLocationString(String locString, ExecutionContext context) {
        if (locString == null || locString.isEmpty()) {
            return null;
        }
        
        try {
            Player player = context.getPlayer();
            if (player == null) {
                return null;
            }
            
            // Split by colon to separate world and coordinates
            String[] parts = locString.split(":");
            
            if (parts.length == 1) {
                // Format: x,y,z (use player's world)
                String[] coords = parts[0].split(",");
                if (coords.length != 3) {
                    return null;
                }
                
                double x = Double.parseDouble(coords[0].trim());
                double y = Double.parseDouble(coords[1].trim());
                double z = Double.parseDouble(coords[2].trim());
                
                return new Location(player.getWorld(), x, y, z);
            } else if (parts.length == 2) {
                // Format: world:x,y,z
                String worldName = parts[0].trim();
                String[] coords = parts[1].split(",");
                if (coords.length != 3) {
                    return null;
                }
                
                double x = Double.parseDouble(coords[0].trim());
                double y = Double.parseDouble(coords[1].trim());
                double z = Double.parseDouble(coords[2].trim());
                
                World world = player.getServer().getWorld(worldName);
                if (world == null) {
                    return null;
                }
                
                return new Location(world, x, y, z);
            }
        } catch (NumberFormatException e) {
            // Invalid number format
            return null;
        } catch (Exception e) {
            // Any other parsing error
            return null;
        }
        
        return null;
    }
}