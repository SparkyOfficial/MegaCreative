package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class PlayCustomSoundAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String soundName = block.getParameter("sound").asString();
            float volume = block.getParameter("volume").asNumber().floatValue();
            float pitch = block.getParameter("pitch").asNumber().floatValue();
            // Для location нужна более сложная логика парсинга
            
            // TODO: Реализуйте логику проигрывания звука
            player.playSound(player.getLocation(), soundName, volume, pitch);
            
            return ExecutionResult.success("Звук проигран.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при проигрывании звука: " + e.getMessage());
        }
    }
}