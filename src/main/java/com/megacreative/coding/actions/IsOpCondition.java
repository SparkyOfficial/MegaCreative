package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class IsOpCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return false;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
        DataValue rawRequired = block.getParameter("required");
        String requiredStr = rawRequired != null ? resolver.resolve(context, rawRequired).asString() : null;
        
        // Если параметр не указан, проверяем просто на OP
        if (requiredStr == null) {
            return player.isOp();
        }
        
        // Если указан параметр, проверяем его значение
        boolean required = Boolean.parseBoolean(requiredStr);
        return player.isOp() == required;
    }
} 