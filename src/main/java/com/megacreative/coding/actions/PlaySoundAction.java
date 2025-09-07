package com.megacreative.coding.actions;

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
 * Unified sound action that supports both basic and advanced sound features
 * Maintains backward compatibility while providing advanced location and parsing capabilities
 */
public class PlaySoundAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        ParameterResolver resolver = new ParameterResolver(context);

        try {
            // Enhanced parameter resolution with defaults for backward compatibility
            String soundStr = resolveParameter(resolver, block, "sound", "minecraft:block.note_block.harp");
            float volume = (float) resolveNumberParameter(resolver, block, "volume", 1.0);
            float pitch = (float) resolveNumberParameter(resolver, block, "pitch", 1.0);
            String locationStr = resolveParameter(resolver, block, "location", "player");

            if (soundStr == null) return;

            // Enhanced sound parsing with fallback to basic enum lookup
            Sound sound = parseSound(soundStr);
            if (sound == null) {
                player.sendMessage("§cНеизвестный звук: " + soundStr);
                return;
            }
            
            // Validate parameters with reasonable limits
            volume = Math.max(0.0f, Math.min(1.0f, volume));
            pitch = Math.max(0.5f, Math.min(2.0f, pitch));
            
            // Enhanced location handling
            Location soundLocation = parseSoundLocation(locationStr, player);
            
            if (soundLocation != null && !locationStr.equals("player")) {
                // Play at specific location (all players can hear)
                player.getWorld().playSound(soundLocation, sound, volume, pitch);
                player.sendMessage("§a🔊 Звук '" + soundStr + "' воспроизведен на локации!");
            } else {
                // Basic behavior: play directly to player
                player.playSound(player.getLocation(), sound, volume, pitch);
                player.sendMessage("§a🔊 Звук '" + soundStr + "' воспроизведен!");
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметрах volume/pitch");
        } catch (Exception e) {
            player.sendMessage("§cОшибка воспроизведения звука: " + e.getMessage());
        }
    }
    
    /**
     * Resolves a text parameter with fallback default
     */
    private String resolveParameter(ParameterResolver resolver, 
                                  CodeBlock block, String paramName, String defaultValue) {
        DataValue rawValue = block.getParameter(paramName);
        if (rawValue == null) return defaultValue;
        
        return resolver.resolve(context, rawValue).asString();
    }
    
    /**
     * Resolves a numeric parameter with fallback default
     */
    private double resolveNumberParameter(ParameterResolver resolver, 
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
     * Enhanced sound parsing with support for multiple formats
     * Supports both short names and full Minecraft sound names
     */
    private Sound parseSound(String soundName) {
        try {
            // First try basic uppercase conversion for backward compatibility
            try {
                return Sound.valueOf(soundName.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Enhanced parsing for advanced usage
                String cleanName = soundName.replace("minecraft:", "");
                String enumName = cleanName.toUpperCase().replace(".", "_");
                
                // Try direct enum lookup
                try {
                    return Sound.valueOf(enumName);
                } catch (IllegalArgumentException e2) {
                    // Try with BLOCK_ prefix for block sounds
                    try {
                        return Sound.valueOf("BLOCK_" + enumName);
                    } catch (IllegalArgumentException e3) {
                        // Try with ENTITY_ prefix for entity sounds
                        try {
                            return Sound.valueOf("ENTITY_" + enumName);
                        } catch (IllegalArgumentException e4) {
                            return null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Advanced location parsing for enhanced functionality
     * Supports: "player", "spawn", "x,y,z" coordinates
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
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
}