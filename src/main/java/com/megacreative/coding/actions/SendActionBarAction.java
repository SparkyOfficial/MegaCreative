package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class SendActionBarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String message = block.getParameter("message").asString();
            int duration = block.getParameter("duration").asNumber().intValue();
            
            // TODO: Реализуйте логику отправки сообщения в ActionBar
            // player.sendActionBar(...);
            
            return ExecutionResult.success("Сообщение в ActionBar отправлено.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при отправке сообщения в ActionBar: " + e.getMessage());
        }
    }
}