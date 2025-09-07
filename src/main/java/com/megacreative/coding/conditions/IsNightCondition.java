package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;

/**
 * Условие для проверки, является ли сейчас ночь в мире.
 */
public class IsNightCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        if (context.getPlayer() == null || context.getPlayer().getWorld() == null) {
            return false;
        }
        
        long time = context.getPlayer().getWorld().getTime();
        // Night is typically between 13000 and 23000 ticks
        return time >= 13000 && time <= 23000;
    }
}
