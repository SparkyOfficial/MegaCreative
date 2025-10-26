package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Action to play a sound for a player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "playSound", displayName = "§bPlay Sound", type = BlockType.ACTION)
public class PlaySoundAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue soundValue = block.getParameter("sound");
            DataValue volumeValue = block.getParameter("volume");
            DataValue pitchValue = block.getParameter("pitch");
            
            if (soundValue == null) {
                return ExecutionResult.error("Missing required parameter: sound");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedSound = resolver.resolve(context, soundValue);
            
            String soundStr = resolvedSound.asString();
            Sound sound;
            
            try {
                sound = Sound.valueOf(soundStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid sound: " + soundStr);
            }
            
            // Get volume (default to 1.0)
            float volume = 1.0f;
            if (volumeValue != null) {
                DataValue resolvedVolume = resolver.resolve(context, volumeValue);
                volume = Math.max(0.0f, Math.min(1.0f, resolvedVolume.asNumber().floatValue()));
            }
            
            // Get pitch (default to 1.0)
            float pitch = 1.0f;
            if (pitchValue != null) {
                DataValue resolvedPitch = resolver.resolve(context, pitchValue);
                pitch = Math.max(0.5f, Math.min(2.0f, resolvedPitch.asNumber().floatValue()));
            }
            
            // Play sound
            player.playSound(player.getLocation(), sound, volume, pitch);
            
            return ExecutionResult.success("Played sound " + sound.name());
        } catch (Exception e) {
            return ExecutionResult.error("Failed to play sound: " + e.getMessage());
        }
    }
}