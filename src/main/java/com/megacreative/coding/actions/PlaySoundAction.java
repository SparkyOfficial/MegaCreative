package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

/**
 * Action for playing a sound to a player.
 * This action retrieves sound parameters and plays the sound to the player.
 */
@BlockMeta(id = "playSound", displayName = "§aPlay Sound", type = BlockType.ACTION)
public class PlaySoundAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get sound parameters
            String soundName = getParameterValue(block, "sound");
            float volume = getFloatParameter(block, "volume", 1.0f);
            float pitch = getFloatParameter(block, "pitch", 1.0f);
            
            if (soundName == null || soundName.isEmpty()) {
                return ExecutionResult.error("Sound name is not configured");
            }

            // Parse sound
            Sound sound = parseSound(soundName);
            if (sound == null) {
                return ExecutionResult.error("Invalid sound name: " + soundName);
            }

            // Play the sound
            player.playSound(player.getLocation(), sound, volume, pitch);
            return ExecutionResult.success("Sound played successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to play sound: " + e.getMessage());
        }
    }
    
    private String getParameterValue(CodeBlock block, String paramName) {
        com.megacreative.coding.values.DataValue value = block.getParameter(paramName);
        return value != null ? value.asString() : null;
    }
    
    private float getFloatParameter(CodeBlock block, String paramName, float defaultValue) {
        com.megacreative.coding.values.DataValue value = block.getParameter(paramName);
        if (value != null && !value.isEmpty()) {
            try {
                return Float.parseFloat(value.asString());
            } catch (NumberFormatException e) {
                // Use default value
            }
        }
        return defaultValue;
    }
    
    private Sound parseSound(String soundName) {
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try with "ENTITY_" prefix for newer sound names
            try {
                return Sound.valueOf("ENTITY_" + soundName.toUpperCase());
            } catch (IllegalArgumentException e2) {
                // Try with "BLOCK_" prefix
                try {
                    return Sound.valueOf("BLOCK_" + soundName.toUpperCase());
                } catch (IllegalArgumentException e3) {
                    return null;
                }
            }
        }
    }
}