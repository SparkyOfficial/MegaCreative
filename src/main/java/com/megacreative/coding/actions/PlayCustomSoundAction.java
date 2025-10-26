package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Action that plays a custom sound with configurable volume and pitch
 */
@BlockMeta(id = "playCustomSound", displayName = "§aPlay Custom Sound", type = BlockType.ACTION)
public class PlayCustomSoundAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        // Get the sound parameters from the block
        DataValue soundValue = block.getParameter("sound");
        DataValue volumeValue = block.getParameter("volume");
        DataValue pitchValue = block.getParameter("pitch");
        
        if (soundValue == null) {
            return ExecutionResult.error("No sound parameter found");
        }
        
        try {
            Sound sound = Sound.valueOf(soundValue.asString().toUpperCase());
            float volume = volumeValue != null ? parseFloat(volumeValue.asString(), 1.0f) : 1.0f;
            float pitch = pitchValue != null ? parseFloat(pitchValue.asString(), 1.0f) : 1.0f;
            
            // Play the custom sound to the player
            Location location = player.getLocation();
            player.playSound(location, sound, volume, pitch);
            
            context.getPlugin().getLogger().fine("Played custom sound '" + sound + "' to player " + player.getName());
            return ExecutionResult.success("Custom sound played successfully");
        } catch (IllegalArgumentException e) {
            return ExecutionResult.error("Invalid sound type: " + soundValue.asString());
        }
    }
    
    private float parseFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}