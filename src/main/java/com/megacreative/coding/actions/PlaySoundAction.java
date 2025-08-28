package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlaySoundAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);

        // Получаем и разрешаем параметры
        DataValue rawSound = block.getParameter("sound");
        DataValue rawVolume = block.getParameter("volume");
        DataValue rawPitch = block.getParameter("pitch");

        if (rawSound == null) return;

        String soundStr = resolver.resolve(context, rawSound).asString();
        String volumeStr = rawVolume != null ? resolver.resolve(context, rawVolume).asString() : "1.0";
        String pitchStr = rawPitch != null ? resolver.resolve(context, rawPitch).asString() : "1.0";

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