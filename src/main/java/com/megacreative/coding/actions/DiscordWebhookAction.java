package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;

/**
 * Действие для отправки сообщений через Discord webhook.
 * Пока что это заглушка, требует реализации интеграции с Discord API.
 */
public class DiscordWebhookAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        context.getPlayer().sendMessage("§cДействие 'DiscordWebhook' еще не реализовано.");
        return ExecutionResult.error("Not implemented yet");
    }
}