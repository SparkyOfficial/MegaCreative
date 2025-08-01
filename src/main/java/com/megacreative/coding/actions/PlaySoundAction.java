package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlaySoundAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        if (player == null || block == null) return;

        String soundName = (String) block.getParameter("sound");
        if (soundName != null) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                String volumeStr = (String) block.getParameter("volume");
                String pitchStr = (String) block.getParameter("pitch");
                
                float volume = volumeStr != null ? Float.parseFloat(volumeStr) : 1.0f;
                float pitch = pitchStr != null ? Float.parseFloat(pitchStr) : 1.0f;
                
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception e) {
                player.sendMessage("§cНеверный звук или параметры звука.");
            }
        }
    }
} 