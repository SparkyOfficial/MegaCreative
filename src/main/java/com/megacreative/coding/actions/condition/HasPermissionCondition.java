package com.megacreative.coding.actions.condition;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta; // Added import
import com.megacreative.coding.BlockType; // Added import
import org.bukkit.entity.Player;

@BlockMeta(id = "hasPermission", displayName = "§aHas Permission", type = BlockType.CONDITION) // Added annotation
public class HasPermissionCondition implements BlockCondition {
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) { // Fixed method signature
        Player player = context.getPlayer();
        
        if (player == null || block == null) return false;
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawPermission = block.getParameter("permission");
        if (rawPermission == null) return false;
        
        String permission = resolver.resolve(context, rawPermission).asString();
        
        return permission != null && player.hasPermission(permission);
    }
}