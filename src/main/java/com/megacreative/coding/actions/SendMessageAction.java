package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class SendMessageAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return;
        
        // Получаем параметр "message" из текущего блока
        String message = (String) block.getParameter("message");
        if (message != null) {
            message = message.replace("%player%", player.getName());
            player.sendMessage(message);
        }
    }
} 