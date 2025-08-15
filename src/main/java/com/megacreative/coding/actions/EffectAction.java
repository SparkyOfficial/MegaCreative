package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;

public class EffectAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawEffect = block.getParameter("effect");
        Object rawDuration = block.getParameter("duration");
        Object rawAmplifier = block.getParameter("amplifier");

        String effectStr = ParameterResolver.resolve(context, rawEffect);
        String durationStr = ParameterResolver.resolve(context, rawDuration);
        String amplifierStr = ParameterResolver.resolve(context, rawAmplifier);

        if (effectStr == null) return;

        try {
            PotionEffectType effectType = PotionEffectType.getByName(effectStr.toUpperCase());
            if (effectType == null) {
                player.sendMessage("§cНеизвестный эффект: " + effectStr);
                return;
            }
            
            int duration = durationStr != null ? Integer.parseInt(durationStr) : 200;
            int amplifier = amplifierStr != null ? Integer.parseInt(amplifierStr) : 0;
            
            PotionEffect effect = new PotionEffect(effectType, duration, amplifier);
            player.addPotionEffect(effect);
            
            player.sendMessage("§a⚡ Эффект '" + effectStr + "' применен на " + (duration / 20) + " секунд!");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметрах duration/amplifier");
        }
    }
} 