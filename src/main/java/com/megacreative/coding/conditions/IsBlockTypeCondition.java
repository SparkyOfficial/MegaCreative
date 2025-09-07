package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;

/**
 * Условие для проверки типа блока.
 */
public class IsBlockTypeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        // TODO: Implement block type check logic
        context.getPlugin().getLogger().warning("IsBlockTypeCondition not implemented yet");
        return false;
    }
} 