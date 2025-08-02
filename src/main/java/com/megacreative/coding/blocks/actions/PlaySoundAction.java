package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.values.TextValue;
import com.megacreative.coding.values.NumberValue;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Optional;

public class PlaySoundAction implements BlockAction {

    private final ParameterArgument soundArgument;
    private final ParameterArgument volumeArgument;
    private final ParameterArgument pitchArgument;

    public PlaySoundAction() {
        this.soundArgument = new ParameterArgument("sound");
        this.volumeArgument = new ParameterArgument("volume");
        this.pitchArgument = new ParameterArgument("pitch");
    }

    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            context.getPlugin().getLogger().warning("PlaySoundAction: Игрок не найден в контексте");
            return;
        }

        if (context.getCurrentBlock() == null) {
            context.getPlugin().getLogger().warning("PlaySoundAction: Текущий блок не найден");
            return;
        }

        // Получаем звук
        Optional<TextValue> soundValueOpt = soundArgument.parse(context.getCurrentBlock());
        if (soundValueOpt.isEmpty()) {
            context.getPlugin().getLogger().warning("PlaySoundAction: Не удалось получить звук");
            return;
        }

        String soundStr = soundValueOpt.get().get(context);
        if (soundStr == null || soundStr.trim().isEmpty()) {
            context.getPlugin().getLogger().warning("PlaySoundAction: Звук пуст");
            return;
        }

        // Получаем громкость (по умолчанию 1.0)
        float volume = 1.0f;
        Optional<TextValue> volumeValueOpt = volumeArgument.parse(context.getCurrentBlock());
        if (volumeValueOpt.isPresent()) {
            String volumeStr = volumeValueOpt.get().get(context);
            if (volumeStr != null && !volumeStr.trim().isEmpty()) {
                try {
                    volume = Float.parseFloat(volumeStr);
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("PlaySoundAction: Неверный формат громкости: " + volumeStr);
                }
            }
        }

        // Получаем высоту тона (по умолчанию 1.0)
        float pitch = 1.0f;
        Optional<TextValue> pitchValueOpt = pitchArgument.parse(context.getCurrentBlock());
        if (pitchValueOpt.isPresent()) {
            String pitchStr = pitchValueOpt.get().get(context);
            if (pitchStr != null && !pitchStr.trim().isEmpty()) {
                try {
                    pitch = Float.parseFloat(pitchStr);
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("PlaySoundAction: Неверный формат высоты тона: " + pitchStr);
                }
            }
        }

        try {
            Sound sound = Sound.valueOf(soundStr.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
            player.sendMessage("§a🔊 Звук '" + soundStr + "' воспроизведен!");
            context.getPlugin().getLogger().info("PlaySoundAction: Звук '" + soundStr + "' воспроизведен для " + player.getName());
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cНеизвестный звук: " + soundStr);
            context.getPlugin().getLogger().warning("PlaySoundAction: Неизвестный звук '" + soundStr + "' для игрока " + player.getName());
        }
    }

    /**
     * Возвращает имя действия
     */
    public String getActionName() {
        return "playSound";
    }

    /**
     * Возвращает описание действия
     */
    public String getDescription() {
        return "Воспроизводит звук для игрока";
    }
} 