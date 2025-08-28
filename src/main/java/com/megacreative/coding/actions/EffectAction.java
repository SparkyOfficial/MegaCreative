package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;

public class EffectAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);

        // Получаем и разрешаем параметры
        DataValue rawEffect = block.getParameter("effect");
        DataValue rawDuration = block.getParameter("duration");
        DataValue rawAmplifier = block.getParameter("amplifier");

        if (rawEffect == null) return;

        String effectStr = resolver.resolve(context, rawEffect).asString();
        String durationStr = rawDuration != null ? resolver.resolve(context, rawDuration).asString() : "100";
        String amplifierStr = rawAmplifier != null ? resolver.resolve(context, rawAmplifier).asString() : "1";

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