package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class DebugLogAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String message = block.getParameter("message").asString();
            String level = block.getParameter("level").asString();
            
            // TODO: Реализуйте логику записи отладочной информации
            // Запись сообщения в лог с указанным уровнем
            
            return ExecutionResult.success("Отладочная информация записана.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при записи отладочной информации: " + e.getMessage());
        }
    }
}