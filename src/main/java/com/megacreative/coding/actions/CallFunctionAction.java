package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class CallFunctionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String functionName = block.getParameter("function_name").asString();
            // TODO: Получите аргументы функции
            // List<Object> args = block.getParameter("args").asList();
            
            // TODO: Реализуйте логику вызова функции
            // Поиск и выполнение ранее определенной функции
            
            return ExecutionResult.success("Функция вызвана.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при вызове функции: " + e.getMessage());
        }
    }
}
