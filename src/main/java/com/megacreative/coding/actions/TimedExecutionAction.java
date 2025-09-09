package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class TimedExecutionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            int delay = block.getParameter("delay").asNumber().intValue();
            boolean repeat = block.getParameter("repeat").asBoolean();
            
            // TODO: Реализуйте логику отложенного выполнения
            // Bukkit.getScheduler().runTaskLater(...);
            
            return ExecutionResult.success("Отложенное выполнение запланировано.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при планировании отложенного выполнения: " + e.getMessage());
        }
    }
}