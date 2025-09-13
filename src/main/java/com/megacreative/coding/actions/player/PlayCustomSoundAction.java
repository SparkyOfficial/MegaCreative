package com.megacreative.coding.actions.player;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlayCustomSoundAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in context");
        }

        try {
            // Get parameters
            DataValue soundValue = block.getParameter("sound");
            DataValue volumeValue = block.getParameter("volume", DataValue.of(1.0));
            DataValue pitchValue = block.getParameter("pitch", DataValue.of(1.0));

            if (soundValue == null || soundValue.isEmpty()) {
                return ExecutionResult.error("Sound parameter is missing");
            }

            // Parse sound
            Sound sound = Sound.valueOf(soundValue.asString().toUpperCase());
            if (sound == null) {
                return ExecutionResult.error("Invalid sound: " + soundValue.asString());
            }

            // Parse volume and pitch
            float volume = volumeValue.asNumber().floatValue();
            float pitch = pitchValue.asNumber().floatValue();

            // Play sound
            player.playSound(player.getLocation(), sound, volume, pitch);

            return ExecutionResult.success("Played sound " + sound.name());
        } catch (Exception e) {
            return ExecutionResult.error("Error playing sound: " + e.getMessage());
        }
    }
}