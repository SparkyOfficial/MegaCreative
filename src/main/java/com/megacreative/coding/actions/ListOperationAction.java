package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class ListOperationAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String listName = block.getParameter("list_name").asString();
            String operation = block.getParameter("operation").asString();
            // TODO: Получите значение для операции
            // Object value = block.getParameter("value").asObject();
            // int index = block.getParameter("index").asNumber().intValue();
            
            // TODO: Реализуйте логику операций со списком
            // add/remove/get/set/size
            
            return ExecutionResult.success("Операция со списком выполнена.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при выполнении операции со списком: " + e.getMessage());
        }
    }
}