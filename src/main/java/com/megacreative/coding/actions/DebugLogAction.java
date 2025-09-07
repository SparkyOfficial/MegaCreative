package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;

/**
 * Действие для записи отладочной информации в лог.
 * Полезно для отладки скриптов и отслеживания их выполнения.
 */
public class DebugLogAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        context.getPlugin().getLogger().info("Debug log action executed");
        return ExecutionResult.success("Debug log recorded");
    }
}