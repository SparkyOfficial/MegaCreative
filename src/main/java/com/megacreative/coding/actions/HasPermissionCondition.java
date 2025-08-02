package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class HasPermissionCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        Object rawPermission = block.getParameter("permission");
        String permission = ParameterResolver.resolve(context, rawPermission);
        
        return permission != null && player.hasPermission(permission);
    }
} 