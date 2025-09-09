package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class MapOperationAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String mapName = block.getParameter("map_name").asString();
            String operation = block.getParameter("operation").asString();
            String key = block.getParameter("key").asString();
            // TODO: Получите значение для операции
            // Object value = block.getParameter("value").asObject();
            
            // TODO: Реализуйте логику операций с картой
            // put/get/remove/keys/values
            
            return ExecutionResult.success("Операция с картой выполнена.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при выполнении операции с картой: " + e.getMessage());
        }
    }
}