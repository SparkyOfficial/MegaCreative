package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WaitAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawTicks = block.getParameter("ticks");

        String ticksStr = ParameterResolver.resolve(context, rawTicks);

        if (ticksStr == null) return;

        try {
            int ticks = Integer.parseInt(ticksStr);
            
            // Запускаем выполнение следующего блока через указанное количество тиков
            Bukkit.getScheduler().runTaskLater(context.getPlugin(), () -> {
                CodeBlock nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    ScriptExecutor executor = new ScriptExecutor(context.getPlugin());
                    ExecutionContext newContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
                    executor.processBlock(nextBlock, newContext);
                }
            }, ticks);
            
            player.sendMessage("§a⏱ Ожидание " + ticks + " тиков...");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметре ticks: " + ticksStr);
        }
    }
} 