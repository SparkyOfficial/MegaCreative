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
    // Removed static field and will use RepeatingTaskManager service instead
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        // Получаем и разрешаем параметры

        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawTicks = block.getParameter("ticks");
        DataValue rawAction = block.getParameter("action");

        if (rawTicks == null) {
            return ExecutionResult.error("Ticks parameter is missing");
        }
        
        DataValue ticksValue = resolver.resolve(context, rawTicks);
        String ticksStr = ticksValue.asString();
        
        if (rawAction != null) {
            DataValue actionValue = resolver.resolve(context, rawAction);
            // The value actionValue.asString() assigned to actionStr is never used
            // String actionStr = actionValue.asString();  // Removed unused assignment
        }

        if (ticksStr == null) {
            return ExecutionResult.error("Ticks parameter is null");
        }

        try {
            int ticks = Integer.parseInt(ticksStr);
            
            // Получаем RepeatingTaskManager из ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            RepeatingTaskManager taskManager = serviceRegistry.getRepeatingTaskManager();
            
            // Останавливаем предыдущую задачу для этого игрока, если она существует
            taskManager.stopRepeatingTask(player.getUniqueId());
            
            // Создаем уникальный идентификатор для задачи
            UUID taskId = UUID.randomUUID();
            
            // Запускаем повторяющуюся задачу
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(context.getPlugin(), () -> {
                try {
                    // Получаем ScriptEngine из ServiceRegistry
                    ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
                    if (scriptEngine == null) {
                        player.sendMessage("§cОшибка: не удалось получить ScriptEngine");
                        taskManager.stopRepeatingTask(player.getUniqueId());
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
                                taskManager.stopRepeatingTask(player.getUniqueId());
                                return null;
                            });
                    }
                } catch (Exception e) {
                    player.sendMessage("§cОшибка в повторяющемся триггере: " + e.getMessage());
                    taskManager.stopRepeatingTask(player.getUniqueId());
                }
            }, ticks, ticks);
            
            // Сохраняем ID задачи через RepeatingTaskManager
            taskManager.startRepeatingTask(player.getUniqueId(), task);
            
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
        // This method should not be static and should use context instead
        // For backward compatibility, we'll keep it but mark it as deprecated
        MegaCreative plugin = MegaCreative.getInstance();
        if (plugin != null && plugin.getServiceRegistry() != null) {
            RepeatingTaskManager taskManager = plugin.getServiceRegistry().getRepeatingTaskManager();
            if (taskManager != null) {
                taskManager.stopRepeatingTask(playerId);
            }
        }
    }
    
    /**
     * Останавливает все повторяющиеся задачи
     * @return Количество остановленных задач
     */
    public static int stopAllRepeatingTasks() {
        // This method should not be static and should use context instead
        // For backward compatibility, we'll keep it but mark it as deprecated
        MegaCreative plugin = MegaCreative.getInstance();
        if (plugin != null && plugin.getServiceRegistry() != null) {
            RepeatingTaskManager taskManager = plugin.getServiceRegistry().getRepeatingTaskManager();
            if (taskManager != null) {
                return taskManager.stopAllRepeatingTasks();
            }
        }
        return 0;
    }
    
    /**
     * Проверяет, есть ли активная повторяющаяся задача для игрока
     */
    public static boolean hasActiveTask(UUID playerId) {
        // This method should not be static and should use context instead
        // For backward compatibility, we'll keep it but mark it as deprecated
        MegaCreative plugin = MegaCreative.getInstance();
        if (plugin != null && plugin.getServiceRegistry() != null) {
            RepeatingTaskManager taskManager = plugin.getServiceRegistry().getRepeatingTaskManager();
            if (taskManager != null) {
                return taskManager.hasActiveTask(playerId);
            }
        }
        return false;
    }
}