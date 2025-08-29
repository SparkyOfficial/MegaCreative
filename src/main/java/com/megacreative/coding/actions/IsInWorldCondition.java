package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class IsInWorldCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawWorldName = block.getParameter("world");
        if (rawWorldName == null) return false;
        
        String worldName = resolver.resolve(rawWorldName).asString();
        
        return worldName != null && player.getWorld().getName().equals(worldName);
    }
} 