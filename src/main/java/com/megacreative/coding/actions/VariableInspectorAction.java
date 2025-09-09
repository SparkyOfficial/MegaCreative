package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class VariableInspectorAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String scope = block.getParameter("scope").asString();
            String filter = block.getParameter("filter").asString();
            
            // TODO: Реализуйте логику инспекции переменных
            // Получение и отображение значений переменных
            
            return ExecutionResult.success("Инспекция переменных выполнена.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при инспекции переменных: " + e.getMessage());
        }
    }
}