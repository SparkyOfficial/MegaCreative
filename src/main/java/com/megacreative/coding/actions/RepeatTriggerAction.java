package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RepeatTriggerAction implements BlockAction {
    private static final Map<UUID, Integer> activeTasks = new HashMap<>();
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        // Получаем и разрешаем параметры
        if (context == null) {
            return ExecutionResult.error("Context is null");
        }
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawTicks = block.getParameter("ticks");
        DataValue rawAction = block.getParameter("action");

        if (rawTicks == null) {
            return ExecutionResult.error("Ticks parameter is missing");
        }
        
        DataValue ticksValue = resolver.resolve(context, rawTicks);
        String ticksStr = ticksValue.asString();
        
        String actionStr = null;
        if (rawAction != null) {
            DataValue actionValue = resolver.resolve(context, rawAction);
            actionStr = actionValue.asString();
        }

        if (ticksStr == null) {
            return ExecutionResult.error("Ticks parameter is null");
        }

        try {
            int ticks = Integer.parseInt(ticksStr);
            
            // Останавливаем предыдущую задачу для этого игрока, если она существует
            stopRepeatingTask(player.getUniqueId());
            
            // Создаем уникальный идентификатор для задачи
            UUID taskId = UUID.randomUUID();
            
            // Запускаем повторяющуюся задачу
            int taskIdBukkit = Bukkit.getScheduler().runTaskTimer(context.getPlugin(), () -> {
                try {
                    // Получаем ScriptEngine из ServiceRegistry
                    ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
                    if (scriptEngine == null) {
                        player.sendMessage("§cОшибка: не удалось получить ScriptEngine");
                        stopRepeatingTask(player.getUniqueId());
                        return;
                    }
                    
                    // Выполняем действие
                    CodeBlock nextBlock = block.getNextBlock();
                    if (nextBlock != null) {
                        ExecutionContext newContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
                        // Используем ScriptEngine для выполнения следующего блока
                        scriptEngine.executeBlockChain(nextBlock, player, "repeat_trigger")
                            .exceptionally(throwable -> {
                                player.sendMessage("§cОшибка в повторяющемся триггере: " + throwable.getMessage());
                                stopRepeatingTask(player.getUniqueId());
                                return null;
                            });
                    }
                } catch (Exception e) {
                    player.sendMessage("§cОшибка в повторяющемся триггере: " + e.getMessage());
                    stopRepeatingTask(player.getUniqueId());
                }
            }, ticks, ticks).getTaskId();
            
            // Сохраняем ID задачи
            activeTasks.put(player.getUniqueId(), taskIdBukkit);
            
            player.sendMessage("§a🔄 Повторяющийся триггер запущен каждые " + ticks + " тиков");
            
            return ExecutionResult.success("Repeat trigger action started");
        } catch (NumberFormatException e) {
            return ExecutionResult.error("Invalid ticks parameter: " + ticksStr);
        }
    }
    
    /**
     * Останавливает повторяющуюся задачу для указанного игрока
     */
    public static void stopRepeatingTask(UUID playerId) {
        Integer taskId = activeTasks.get(playerId);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            activeTasks.remove(playerId);
        }
    }
    
    /**
     * Останавливает все повторяющиеся задачи
     * @return Количество остановленных задач
     */
    public static int stopAllRepeatingTasks() {
        int count = activeTasks.size();
        for (Integer taskId : activeTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        activeTasks.clear();
        return count;
    }
    
    /**
     * Проверяет, есть ли активная повторяющаяся задача для игрока
     */
    public static boolean hasActiveTask(UUID playerId) {
        return activeTasks.containsKey(playerId);
    }
}