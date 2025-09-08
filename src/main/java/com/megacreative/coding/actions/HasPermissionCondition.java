package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class HasPermissionCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawPermission = block.getParameter("permission");
        if (rawPermission == null) return false;
        
        String permission = resolver.resolve(context, rawPermission).asString();
        
        return permission != null && player.hasPermission(permission);
    }
}