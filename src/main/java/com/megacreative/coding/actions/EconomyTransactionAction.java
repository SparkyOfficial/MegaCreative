package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;

/**
 * Действие для выполнения экономических транзакций (например, передача денег между игроками).
 * Пока что это заглушка, требует интеграции с экономической системой.
 */
public class EconomyTransactionAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        context.getPlayer().sendMessage("§cДействие 'EconomyTransaction' еще не реализовано.");
        return ExecutionResult.error("Not implemented yet");
    }
}