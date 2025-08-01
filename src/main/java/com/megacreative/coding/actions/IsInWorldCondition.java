package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class IsInWorldCondition implements BlockCondition {
    @Override
    public boolean check(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        String worldName = (String) block.getParameter("world");
        return worldName != null && player.getWorld().getName().equals(worldName);
    }
} 