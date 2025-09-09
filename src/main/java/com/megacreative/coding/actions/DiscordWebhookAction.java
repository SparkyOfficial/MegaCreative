package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class DiscordWebhookAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String webhookUrl = block.getParameter("webhook_url").asString();
            String message = block.getParameter("message").asString();
            String username = block.getParameter("username").asString();
            
            // TODO: Реализуйте логику отправки сообщения в Discord
            // HTTP-запрос к webhook URL
            
            return ExecutionResult.success("Сообщение отправлено в Discord.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при отправке сообщения в Discord: " + e.getMessage());
        }
    }
}