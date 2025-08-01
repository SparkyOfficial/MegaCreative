package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class GetPlayerNameAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        if (player == null || block == null) return;
        
        String playerName = player.getName();
        context.setVariable("lastValue", playerName);
        
        player.sendMessage("§aИмя игрока: " + playerName);
    }
} 