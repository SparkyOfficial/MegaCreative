package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Action for playing a sound to a player.
 * This action retrieves sound parameters from the block and plays the sound.
 */
public class PlaySoundAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the sound parameter from the block
            DataValue soundValue = block.getParameter("sound");
            if (soundValue == null) {
                return ExecutionResult.error("Sound parameter is missing");
            }

            // Resolve any placeholders in the sound name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedSound = resolver.resolve(context, soundValue);
            
            // Parse sound parameters
            String soundName = resolvedSound.asString();
            if (soundName == null || soundName.isEmpty()) {
                return ExecutionResult.error("Sound name is empty or null");
            }

            // Get optional volume and pitch parameters
            float volume = 1.0f;
            float pitch = 1.0f;
            
            DataValue volumeValue = block.getParameter("volume");
            if (volumeValue != null) {
                DataValue resolvedVolume = resolver.resolve(context, volumeValue);
                try {
                    volume = resolvedVolume.asNumber().floatValue();
                } catch (NumberFormatException e) {
                    // Use default volume if parsing fails
                }
            }
            
            DataValue pitchValue = block.getParameter("pitch");
            if (pitchValue != null) {
                DataValue resolvedPitch = resolver.resolve(context, pitchValue);
                try {
                    pitch = resolvedPitch.asNumber().floatValue();
                } catch (NumberFormatException e) {
                    // Use default pitch if parsing fails
                }
            }

            // Play the sound
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, volume, pitch);
                return ExecutionResult.success("Sound played successfully");
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid sound name: " + soundName);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to play sound: " + e.getMessage());
        }
    }
}