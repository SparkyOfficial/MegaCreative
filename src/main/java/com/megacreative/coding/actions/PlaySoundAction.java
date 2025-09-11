package com.megacreative.coding.actions;

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
 * Action for playing a sound to a player.
 * This action retrieves sound parameters from the container configuration and plays the sound.
 */
public class PlaySoundAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get sound parameters from the container configuration
            PlaySoundParams params = getSoundParamsFromContainer(block, context);
            
            if (params.soundNameStr == null || params.soundNameStr.isEmpty()) {
                return ExecutionResult.error("Sound is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue soundNameVal = DataValue.of(params.soundNameStr);
            DataValue resolvedSoundName = resolver.resolve(context, soundNameVal);
            
            // Parse parameters
            String soundName = resolvedSoundName.asString();
            float volume = params.volume;
            float pitch = params.pitch;

            // Parse the sound
            Sound sound;
            try {
                sound = Sound.valueOf(soundName.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default sound if parsing fails
                sound = Sound.ENTITY_PLAYER_LEVELUP;
            }

            // Play the sound
            player.playSound(player.getLocation(), sound, volume, pitch);
            return ExecutionResult.success("Sound played successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to play sound: " + e.getMessage());
        }
    }
    
    /**
     * Gets sound parameters from the container configuration
     */
    private PlaySoundParams getSoundParamsFromContainer(CodeBlock block, ExecutionContext context) {
        PlaySoundParams params = new PlaySoundParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get sound name from the sound slot
                Integer soundSlot = slotResolver.apply("sound_slot");
                if (soundSlot != null) {
                    ItemStack soundItem = block.getConfigItem(soundSlot);
                    if (soundItem != null && soundItem.hasItemMeta()) {
                        // Extract sound name from item
                        params.soundNameStr = getSoundNameFromItem(soundItem);
                    }
                }
                
                // Get volume from the volume slot
                Integer volumeSlot = slotResolver.apply("volume_slot");
                if (volumeSlot != null) {
                    ItemStack volumeItem = block.getConfigItem(volumeSlot);
                    if (volumeItem != null && volumeItem.hasItemMeta()) {
                        // Extract volume from item
                        params.volume = getVolumeFromItem(volumeItem, 1.0f);
                    }
                }
                
                // Get pitch from the pitch slot
                Integer pitchSlot = slotResolver.apply("pitch_slot");
                if (pitchSlot != null) {
                    ItemStack pitchItem = block.getConfigItem(pitchSlot);
                    if (pitchItem != null && pitchItem.hasItemMeta()) {
                        // Extract pitch from item
                        params.pitch = getPitchFromItem(pitchItem, 1.0f);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting sound parameters from container in PlaySoundAction: " + e.getMessage());
        }
        
        // Set defaults if not configured
        if (params.soundNameStr == null || params.soundNameStr.isEmpty()) {
            params.soundNameStr = "ENTITY_PLAYER_LEVELUP";
        }
        
        return params;
    }
    
    /**
     * Extracts sound name from an item
     */
    private String getSoundNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the sound name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts volume from an item
     */
    private float getVolumeFromItem(ItemStack item, float defaultVolume) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse volume from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Float.parseFloat(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultVolume;
        }
    }
    
    /**
     * Extracts pitch from an item
     */
    private float getPitchFromItem(ItemStack item, float defaultPitch) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse pitch from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Float.parseFloat(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultPitch;
        }
    }
    
    /**
     * Helper class to hold sound parameters
     */
    private static class PlaySoundParams {
        String soundNameStr = "";
        float volume = 1.0f;
        float pitch = 1.0f;
    }
}