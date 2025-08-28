package com.megacreative.coding.actions.advanced;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Advanced sound action that supports custom sounds with volume, pitch, and location control
 * Demonstrates sophisticated parameter handling and audio system integration
 */
public class PlayCustomSoundAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);

        try {
            // Resolve parameters with defaults
            String soundName = resolveParameter(resolver, context, block, "sound", "minecraft:block.note_block.harp");
            float volume = (float) resolveNumberParameter(resolver, context, block, "volume", 1.0);
            float pitch = (float) resolveNumberParameter(resolver, context, block, "pitch", 1.0);
            String locationStr = resolveParameter(resolver, context, block, "location", "player");
            
            // Validate and convert sound name
            Sound sound = parseSound(soundName);
            if (sound == null) {
                player.sendMessage("§cInvalid sound: " + soundName);
                return;
            }
            
            // Validate audio parameters
            volume = Math.max(0.0f, Math.min(1.0f, volume));
            pitch = Math.max(0.5f, Math.min(2.0f, pitch));
            
            // Determine playback location
            Location soundLocation = parseSoundLocation(locationStr, player);
            
            // Play the sound
            if (soundLocation != null) {
                // Play at specific location (all players can hear)
                player.getWorld().playSound(soundLocation, sound, volume, pitch);
                
                // Debug feedback for developer
                if (context.isDebugMode()) {
                    player.sendMessage("§7[DEBUG] Played sound '" + soundName + "' at " + 
                                     locationStr + " (vol:" + volume + ", pitch:" + pitch + ")");
                }
            } else {
                // Play directly to player
                player.playSound(player.getLocation(), sound, volume, pitch);
                
                if (context.isDebugMode()) {
                    player.sendMessage("§7[DEBUG] Played personal sound '" + soundName + 
                                     "' (vol:" + volume + ", pitch:" + pitch + ")");
                }
            }
            
        } catch (Exception e) {
            player.sendMessage("§cError playing sound: " + e.getMessage());
            if (context.isDebugMode()) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Resolves a text parameter with fallback default
     */
    private String resolveParameter(ParameterResolver resolver, ExecutionContext context, 
                                  CodeBlock block, String paramName, String defaultValue) {
        DataValue rawValue = block.getParameter(paramName);
        if (rawValue == null) return defaultValue;
        
        return resolver.resolve(context, rawValue).asString();
    }
    
    /**
     * Resolves a numeric parameter with fallback default
     */
    private double resolveNumberParameter(ParameterResolver resolver, ExecutionContext context, 
                                        CodeBlock block, String paramName, double defaultValue) {
        DataValue rawValue = block.getParameter(paramName);
        if (rawValue == null) return defaultValue;
        
        try {
            return resolver.resolve(context, rawValue).asNumber().doubleValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Parses sound name to Bukkit Sound enum
     * Supports both short names (like "note_block.harp") and full names
     */
    private Sound parseSound(String soundName) {
        try {
            // Remove minecraft: prefix if present
            String cleanName = soundName.replace("minecraft:", "");
            
            // Convert to uppercase and replace dots with underscores for enum lookup
            String enumName = cleanName.toUpperCase().replace(".", "_");
            
            // Try direct enum lookup first
            try {
                return Sound.valueOf(enumName);
            } catch (IllegalArgumentException e) {
                // Try with BLOCK_ prefix for block sounds
                try {
                    return Sound.valueOf("BLOCK_" + enumName);
                } catch (IllegalArgumentException e2) {
                    // Try with ENTITY_ prefix for entity sounds
                    try {
                        return Sound.valueOf("ENTITY_" + enumName);
                    } catch (IllegalArgumentException e3) {
                        // Return null if no valid sound found
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Parses location string to actual location
     * Supports: "player", "spawn", "x,y,z", "${variable:location}"
     */
    private Location parseSoundLocation(String locationStr, Player player) {
        if (locationStr == null || locationStr.isEmpty() || locationStr.equals("player")) {
            return null; // Will play to player directly
        }
        
        try {
            if (locationStr.equals("spawn")) {
                return player.getWorld().getSpawnLocation();
            }
            
            // Parse coordinate format: "x,y,z"
            if (locationStr.contains(",")) {
                String[] coords = locationStr.split(",");
                if (coords.length == 3) {
                    double x = Double.parseDouble(coords[0].trim());
                    double y = Double.parseDouble(coords[1].trim());
                    double z = Double.parseDouble(coords[2].trim());
                    return new Location(player.getWorld(), x, y, z);
                }
            }
            
            // Could be extended to support variable resolution for complex locations
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
}