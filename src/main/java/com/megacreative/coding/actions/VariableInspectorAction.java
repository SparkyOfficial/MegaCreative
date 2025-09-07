package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;

/**
 * Действие для инспекции значений переменных.
 * Позволяет просматривать текущие значения переменных во время выполнения скрипта.
 */
public class VariableInspectorAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        context.getPlayer().sendMessage("§cДействие 'VariableInspector' еще не реализовано.");
        return ExecutionResult.error("Not implemented yet");
    }
}