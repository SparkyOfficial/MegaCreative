package com.megacreative.coding.actions.condition;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta; // Added import
import com.megacreative.coding.BlockType; // Added import
import org.bukkit.entity.Player;

@BlockMeta(id = "isInWorld", displayName = "§aIs In World", type = BlockType.CONDITION) // Added annotation
public class IsInWorldCondition implements BlockCondition {
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) { // Fixed method signature
        Player player = context.getPlayer();
        
        if (player == null || block == null) return false;
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawWorldName = block.getParameter("world");
        if (rawWorldName == null) return false;
        
        String worldName = resolver.resolve(context, rawWorldName).asString();
        
        return worldName != null && player.getWorld().getName().equals(worldName);
    }
}