package com.megacreative.coding.actions.player;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * 🎆 ENHANCED: Action for playing sounds to players
 * Supports both container-based configuration and parameter-based configuration
 */
public class PlaySoundAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get sound parameters from configuration
            SoundParams params = getSoundParamsFromContainer(block, context);
            
            if (params.soundStr == null || params.soundStr.isEmpty()) {
                return ExecutionResult.error("Sound is not configured");
            }

            // Resolve any placeholders in the sound name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue soundValue = DataValue.of(params.soundStr);
            DataValue resolvedSound = resolver.resolve(context, soundValue);
            
            String soundName = resolvedSound.asString();
            if (soundName == null || soundName.isEmpty()) {
                return ExecutionResult.error("Invalid sound name");
            }

            // Parse sound name
            Sound sound = parseSound(soundName);
            if (sound == null) {
                return ExecutionResult.error("Unknown sound: " + soundName);
            }

            // Validate and constrain parameters
            float volume = Math.max(0.0f, Math.min(1.0f, params.volume));
            float pitch = Math.max(0.5f, Math.min(2.0f, params.pitch));

            // Play the sound
            player.playSound(player.getLocation(), sound, volume, pitch);
            
            return ExecutionResult.success("Sound '" + soundName + "' played successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to play sound: " + e.getMessage());
        }
    }
    
    /**
     * 🎆 ENHANCED: Gets sound parameters from container configuration with fallbacks
     */
    private SoundParams getSoundParamsFromContainer(CodeBlock block, ExecutionContext context) {
        SoundParams params = new SoundParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get sound from the sound slot
                Integer soundSlot = slotResolver.apply("sound");
                if (soundSlot != null) {
                    ItemStack soundItem = block.getConfigItem(soundSlot);
                    if (soundItem != null && soundItem.hasItemMeta()) {
                        params.soundStr = getSoundFromItem(soundItem);
                    }
                }
                
                // Get volume from the volume slot
                Integer volumeSlot = slotResolver.apply("volume");
                if (volumeSlot != null) {
                    ItemStack volumeItem = block.getConfigItem(volumeSlot);
                    if (volumeItem != null && volumeItem.hasItemMeta()) {
                        params.volume = getFloatFromItem(volumeItem, 1.0f);
                    }
                }
                
                // Get pitch from the pitch slot
                Integer pitchSlot = slotResolver.apply("pitch");
                if (pitchSlot != null) {
                    ItemStack pitchItem = block.getConfigItem(pitchSlot);
                    if (pitchItem != null && pitchItem.hasItemMeta()) {
                        params.pitch = getFloatFromItem(pitchItem, 1.0f);
                    }
                }
            }
            
            // 🎆 ENHANCED: Fallback to parameter-based configuration
            DataValue soundParam = block.getParameter("sound");
            DataValue volumeParam = block.getParameter("volume");
            DataValue pitchParam = block.getParameter("pitch");
            
            if (params.soundStr == null && soundParam != null && !soundParam.isEmpty()) {
                params.soundStr = soundParam.asString();
            }
            
            if (volumeParam != null && !volumeParam.isEmpty()) {
                try {
                    params.volume = Float.parseFloat(volumeParam.asString());
                } catch (NumberFormatException e) {
                    // Use default volume
                }
            }
            
            if (pitchParam != null && !pitchParam.isEmpty()) {
                try {
                    params.pitch = Float.parseFloat(pitchParam.asString());
                } catch (NumberFormatException e) {
                    // Use default pitch
                }
            }
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting sound parameters from container: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts sound name from an item
     */
    private String getSoundFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the sound name
                return org.bukkit.ChatColor.stripColor(displayName).trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts float value from an item
     */
    private float getFloatFromItem(ItemStack item, float defaultValue) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                try {
                    String cleanName = org.bukkit.ChatColor.stripColor(displayName).trim();
                    return Float.parseFloat(cleanName);
                } catch (NumberFormatException e) {
                    // Use default value if parsing fails
                }
            }
        }
        return defaultValue;
    }
    
    /**
     * 🎆 ENHANCED: Parse sound name with support for both enum names and namespaced keys
     */
    private Sound parseSound(String soundName) {
        if (soundName == null || soundName.isEmpty()) {
            return null;
        }
        
        try {
            // Try parsing as direct enum name first
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try common variations and mappings
            String upperName = soundName.toUpperCase().replace(".", "_").replace(":", "_");
            
            // Common sound mappings
            switch (upperName) {
                case "CLICK":
                case "UI_CLICK":
                    return Sound.UI_BUTTON_CLICK;
                case "PLING":
                case "NOTE":
                    return Sound.BLOCK_NOTE_BLOCK_PLING;
                case "POP":
                    return Sound.ENTITY_ITEM_PICKUP;
                case "LEVEL_UP":
                    return Sound.ENTITY_PLAYER_LEVELUP;
                case "SUCCESS":
                    return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                case "ERROR":
                case "FAIL":
                    return Sound.BLOCK_NOTE_BLOCK_BASS;
                case "AMBIENT_CAVE":
                    return Sound.AMBIENT_CAVE;
                default:
                    // Try with common prefixes
                    try {
                        return Sound.valueOf("BLOCK_NOTE_BLOCK_" + upperName);
                    } catch (IllegalArgumentException e2) {
                        try {
                            return Sound.valueOf("ENTITY_" + upperName);
                        } catch (IllegalArgumentException e3) {
                            try {
                                return Sound.valueOf("UI_" + upperName);
                            } catch (IllegalArgumentException e4) {
                                return null;
                            }
                        }
                    }
            }
        }
    }
    
    /**
     * Helper class to hold sound parameters
     */
    private static class SoundParams {
        String soundStr = "";
        float volume = 1.0f;
        float pitch = 1.0f;
    }
}