package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;

/**
 * Действие для выполнения альтернативной ветки в условных конструкциях.
 * Используется в паре с условными блоками для реализации логики "иначе".
 */
public class ElseAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        // Else action typically doesn't perform any specific action itself
        // It's a control flow marker that affects script execution logic
        context.getPlugin().getLogger().info("Else block executed");
        return ExecutionResult.success("Else block executed");
    }
}