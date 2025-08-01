package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class SendMessageAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return;
        
        // Получаем параметр "message" из текущего блока и разрешаем его
        Object rawMessage = block.getParameter("message");
        String resolvedMessage = ParameterResolver.resolve(context, rawMessage).toString();
        
        if (resolvedMessage != null) {
            resolvedMessage = resolvedMessage.replace("%player%", player.getName());
            player.sendMessage(resolvedMessage);
        }
    }
} 