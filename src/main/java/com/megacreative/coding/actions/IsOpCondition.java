package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class IsOpCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        Object rawRequired = block.getParameter("required");
        String requiredStr = ParameterResolver.resolve(context, rawRequired);
        
        // Если параметр не указан, проверяем просто на OP
        if (requiredStr == null) {
            return player.isOp();
        }
        
        // Если указан параметр, проверяем его значение
        boolean required = Boolean.parseBoolean(requiredStr);
        return player.isOp() == required;
    }
} 