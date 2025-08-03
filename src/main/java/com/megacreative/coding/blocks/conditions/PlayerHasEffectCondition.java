package com.megacreative.coding.blocks.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;

public class PlayerHasEffectCondition implements BlockCondition {
    
    private final Argument<TextValue> effectTypeArgument;
    
    public PlayerHasEffectCondition() {
        this.effectTypeArgument = new ParameterArgument("effectType");
    }
    
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        // Получаем тип эффекта
        TextValue effectTypeValue = effectTypeArgument.parse(context.getCurrentBlock()).orElse(null);
        if (effectTypeValue == null) {
            return false;
        }
        
        try {
            String effectType = effectTypeValue.get(context);
            PotionEffectType potionEffectType = PotionEffectType.getByName(effectType.toUpperCase());
            
            if (potionEffectType == null) {
                player.sendMessage("§c✗ Неверный тип эффекта: " + effectType);
                return false;
            }
            
            return player.hasPotionEffect(potionEffectType);
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Ошибка в условии: " + e.getMessage());
            return false;
        }
    }
} 