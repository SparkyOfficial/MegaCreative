package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class ClearInventoryAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        player.getInventory().clear();
        player.sendMessage("§a✓ Инвентарь очищен");
    }
} 