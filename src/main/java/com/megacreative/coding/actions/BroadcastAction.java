package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BroadcastAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (block == null) return;
        
        String message = (String) block.getParameter("message");
        if (message != null) {
            if (player != null) {
                message = message.replace("%player%", player.getName());
            }
            Bukkit.broadcastMessage(message);
        }
    }
} 