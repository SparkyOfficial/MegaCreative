package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.megacreative.core.ServiceRegistry;

public class WaitAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null || block == null) return;

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawTicks = block.getParameter("ticks");

        if (rawTicks == null) return;

        DataValue ticksValue = resolver.resolve(context, rawTicks);
        String ticksStr = ticksValue.asString();

        if (ticksStr == null) return;

        try {
            int ticks = Integer.parseInt(ticksStr);
            
            // Получаем ScriptEngine из ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            ScriptEngine scriptEngine = serviceRegistry.getService(ScriptEngine.class);
            
            if (scriptEngine == null) {
                player.sendMessage("§cОшибка: не удалось получить ScriptEngine");
                return;
            }
            
            // Запускаем выполнение следующего блока через указанное количество тиков
            Bukkit.getScheduler().runTaskLater(context.getPlugin(), () -> {
                CodeBlock nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    // Используем ScriptEngine для выполнения следующего блока
                    scriptEngine.executeScript(nextBlock, player, "after_wait")
                        .exceptionally(throwable -> {
                            player.sendMessage("§cОшибка при выполнении после ожидания: " + throwable.getMessage());
                            return null;
                        });
                }
            }, ticks);
            
            player.sendMessage("§a⏱ Ожидание " + ticks + " тиков...");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметре ticks: " + ticksStr);
        }
    }
} 