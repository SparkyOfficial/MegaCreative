package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.services.RepeatingTaskManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class RepeatTriggerAction implements BlockAction {
    
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        

        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawTicks = block.getParameter("ticks");
        DataValue rawAction = block.getParameter("action");

        if (rawTicks == null) {
            return ExecutionResult.error("Ticks parameter is missing");
        }
        
        DataValue ticksValue = resolver.resolve(context, rawTicks);
        String ticksStr = ticksValue.asString();
        
        // Fix for Qodana issue: Condition ticksStr == null is always false
        // This was a false positive - we need to properly check for empty strings
        if (ticksStr.isEmpty()) {
            return ExecutionResult.error("Ticks parameter is null");
        }
        
        if (rawAction != null) {
            DataValue actionValue = resolver.resolve(context, rawAction);
            
            
        }

        try {
            int ticks = Integer.parseInt(ticksStr);
            
            
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            RepeatingTaskManager taskManager = serviceRegistry.getRepeatingTaskManager();
            
            
            taskManager.stopRepeatingTask(player.getUniqueId());
            
            
            UUID taskId = UUID.randomUUID();
            
            
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(context.getPlugin(), () -> {
                try {
                    
                    ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
                    if (scriptEngine == null) {
                        player.sendMessage("§cОшибка: не удалось получить ScriptEngine");
                        taskManager.stopRepeatingTask(player.getUniqueId());
                        return;
                    }
                    
                    
                    CodeBlock nextBlock = block.getNextBlock();
                    if (nextBlock != null) {
                        ExecutionContext newContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
                        
                        scriptEngine.executeBlockChain(nextBlock, player, "repeat_trigger")
                            .exceptionally(throwable -> {
                                player.sendMessage("§cОшибка в повторяющемся триггере: " + throwable.getMessage());
                                taskManager.stopRepeatingTask(player.getUniqueId());
                                return null;
                            });
                    }
                } catch (Exception e) {
                    player.sendMessage("§cОшибка в повторяющемся триггере: " + e.getMessage());
                    taskManager.stopRepeatingTask(player.getUniqueId());
                }
            }, ticks, ticks);
            
            
            taskManager.startRepeatingTask(player.getUniqueId(), task);
            
            player.sendMessage("§a🔄 Повторяющийся триггер запущен каждые " + ticks + " тиков");
            
            return ExecutionResult.success("Repeat trigger action started");
        } catch (NumberFormatException e) {
            return ExecutionResult.error("Invalid ticks parameter: " + ticksStr);
        }
    }
    
    /**
     * Останавливает повторяющуюся задачу для указанного игрока
     * @deprecated Use RepeatingTaskManager service directly through ServiceRegistry
     */
    @Deprecated
    public static void stopRepeatingTask(UUID playerId) {
        
    }
    
    /**
     * Останавливает все повторяющиеся задачи
     * @return Количество остановленных задач
     * @deprecated Use RepeatingTaskManager service directly through ServiceRegistry
     */
    @Deprecated
    public static int stopAllRepeatingTasks() {
        
        return 0;
    }
    
    /**
     * Проверяет, есть ли активная повторяющаяся задача для игрока
     * @deprecated Use RepeatingTaskManager service directly through ServiceRegistry
     */
    @Deprecated
    public static boolean hasActiveTask(UUID playerId) {
        
        return false;
    }
}