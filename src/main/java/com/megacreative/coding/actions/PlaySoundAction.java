package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlaySoundAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawSound = block.getParameter("sound");
        Object rawVolume = block.getParameter("volume");
        Object rawPitch = block.getParameter("pitch");

        String soundStr = ParameterResolver.resolve(context, rawSound);
        String volumeStr = ParameterResolver.resolve(context, rawVolume);
        String pitchStr = ParameterResolver.resolve(context, rawPitch);

        if (soundStr == null) return;

        try {
            Sound sound = Sound.valueOf(soundStr.toUpperCase());
            float volume = volumeStr != null ? Float.parseFloat(volumeStr) : 1.0f;
            float pitch = pitchStr != null ? Float.parseFloat(pitchStr) : 1.0f;
            
            player.playSound(player.getLocation(), sound, volume, pitch);
            player.sendMessage("§a🔊 Звук '" + soundStr + "' воспроизведен!");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметрах volume/pitch");
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cНеизвестный звук: " + soundStr);
        }
    }
} 