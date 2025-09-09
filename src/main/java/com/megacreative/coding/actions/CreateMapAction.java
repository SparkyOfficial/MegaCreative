package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class CreateMapAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String mapName = block.getParameter("map_name").asString();
            
            // TODO: Реализуйте логику создания карты (словаря)
            // Создание переменной-карты в менеджере переменных
            
            return ExecutionResult.success("Карта создана.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при создании карты: " + e.getMessage());
        }
    }
}