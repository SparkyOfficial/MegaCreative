package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class CreateListAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String listName = block.getParameter("list_name").asString();
            // TODO: Получите начальные значения списка
            // List<Object> initialValues = block.getParameter("initial_values").asList();
            
            // TODO: Реализуйте логику создания списка
            // Создание переменной-списка в менеджере переменных
            
            return ExecutionResult.success("Список создан.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при создании списка: " + e.getMessage());
        }
    }
}