package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class ExecuteAsyncCommandAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String command = block.getParameter("command").asString();
            int delay = block.getParameter("delay").asNumber().intValue();
            
            // TODO: Реализуйте логику асинхронного выполнения команды
            // Bukkit.getScheduler().runTaskLaterAsynchronously(...);
            
            return ExecutionResult.success("Команда будет выполнена асинхронно.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при асинхронном выполнении команды: " + e.getMessage());
        }
    }
}