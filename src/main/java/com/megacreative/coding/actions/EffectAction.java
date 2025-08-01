package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;

public class EffectAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        if (player == null || block == null) return;

        String effectName = (String) block.getParameter("effect");
        if (effectName != null) {
            try {
                PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
                String durationStr = (String) block.getParameter("duration");
                String amplifierStr = (String) block.getParameter("amplifier");
                
                int duration = durationStr != null ? Integer.parseInt(durationStr) : 200;
                int amplifier = amplifierStr != null ? Integer.parseInt(amplifierStr) : 0;
                
                if (effectType != null) {
                    player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                } else {
                    player.sendMessage("§cНеизвестный эффект: " + effectName);
                }
            } catch (Exception e) {
                player.sendMessage("§cОшибка в параметрах эффекта.");
            }
        }
    }
} 