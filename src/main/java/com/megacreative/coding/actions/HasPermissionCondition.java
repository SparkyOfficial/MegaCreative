package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class HasPermissionCondition implements BlockCondition {
    @Override
    public boolean check(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        String permission = (String) block.getParameter("permission");
        return permission != null && player.hasPermission(permission);
    }
} 