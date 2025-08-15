package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;

import java.util.Optional;

public class EffectAction implements BlockAction {

    private final ParameterArgument effectArgument;
    private final ParameterArgument durationArgument;
    private final ParameterArgument amplifierArgument;

    public EffectAction() {
        this.effectArgument = new ParameterArgument("effect");
        this.durationArgument = new ParameterArgument("duration");
        this.amplifierArgument = new ParameterArgument("amplifier");
    }

    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            context.getPlugin().getLogger().warning("EffectAction: Игрок не найден в контексте");
            return;
        }

        if (context.getCurrentBlock() == null) {
            context.getPlugin().getLogger().warning("EffectAction: Текущий блок не найден");
            return;
        }

        // Получаем эффект
        Optional<TextValue> effectValueOpt = effectArgument.parse(context.getCurrentBlock());
        if (effectValueOpt.isEmpty()) {
            context.getPlugin().getLogger().warning("EffectAction: Не удалось получить эффект");
            return;
        }

        String effectStr = effectValueOpt.get().get(context);
        if (effectStr == null || effectStr.trim().isEmpty()) {
            context.getPlugin().getLogger().warning("EffectAction: Эффект пуст");
            return;
        }

        // Получаем длительность (по умолчанию 200 тиков = 10 секунд)
        int duration = 200;
        Optional<TextValue> durationValueOpt = durationArgument.parse(context.getCurrentBlock());
        if (durationValueOpt.isPresent()) {
            String durationStr = durationValueOpt.get().get(context);
            if (durationStr != null && !durationStr.trim().isEmpty()) {
                try {
                    duration = Integer.parseInt(durationStr);
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("EffectAction: Неверный формат длительности: " + durationStr);
                }
            }
        }

        // Получаем усилитель (по умолчанию 0)
        int amplifier = 0;
        Optional<TextValue> amplifierValueOpt = amplifierArgument.parse(context.getCurrentBlock());
        if (amplifierValueOpt.isPresent()) {
            String amplifierStr = amplifierValueOpt.get().get(context);
            if (amplifierStr != null && !amplifierStr.trim().isEmpty()) {
                try {
                    amplifier = Integer.parseInt(amplifierStr);
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("EffectAction: Неверный формат усилителя: " + amplifierStr);
                }
            }
        }

        try {
            PotionEffectType effectType = PotionEffectType.getByName(effectStr.toUpperCase());
            if (effectType == null) {
                player.sendMessage("§cНеизвестный эффект: " + effectStr);
                context.getPlugin().getLogger().warning("EffectAction: Неизвестный эффект '" + effectStr + "' для игрока " + player.getName());
                return;
            }
            
            PotionEffect effect = new PotionEffect(effectType, duration, amplifier);
            player.addPotionEffect(effect);
            
            player.sendMessage("§a⚡ Эффект '" + effectStr + "' применен на " + (duration / 20) + " секунд!");
            context.getPlugin().getLogger().info("EffectAction: Эффект '" + effectStr + "' применен к " + player.getName());
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка применения эффекта: " + e.getMessage());
            context.getPlugin().getLogger().severe("EffectAction: Ошибка применения эффекта '" + effectStr + "' к " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Возвращает имя действия
     */
    public String getActionName() {
        return "effect";
    }

    /**
     * Возвращает описание действия
     */
    public String getDescription() {
        return "Применяет эффект зелья к игроку";
    }
} 