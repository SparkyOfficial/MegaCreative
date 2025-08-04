package com.megacreative.coding.core;

import com.megacreative.coding.blocks.Block;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Класс для управления отложенным выполнением скриптов.
 */
public class ScriptScheduler {
    private final JavaPlugin plugin;
    private final ScriptManager scriptManager;
    private final Map<String, BukkitTask> delayedTasks = new ConcurrentHashMap<>();
    private final Map<String, BukkitTask> repeatingTasks = new ConcurrentHashMap<>();
    
    public ScriptScheduler(JavaPlugin plugin, ScriptManager scriptManager) {
        this.plugin = plugin;
        this.scriptManager = scriptManager;
    }
    
    /**
     * Запускает выполнение блока с задержкой.
     * 
     * @param block Блок для выполнения
     * @param player Игрок, от имени которого выполняется блок
     * @param delayTicks Задержка в тиках (20 тиков = 1 секунда)
     * @param taskId Уникальный идентификатор задачи (если null, генерируется автоматически)
     * @return Идентификатор задачи
     */
    public String scheduleDelayed(Block block, Player player, long delayTicks, String taskId) {
        if (taskId == null) {
            taskId = "delayed_" + UUID.randomUUID().toString();
        } else if (delayedTasks.containsKey(taskId) || repeatingTasks.containsKey(taskId)) {
            throw new IllegalArgumentException("Задача с таким ID уже существует: " + taskId);
        }
        
        // Создаем задачу
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Выполняем блок
            scriptManager.executeScript("scheduled_" + taskId, block, player, null);
            
            // Удаляем задачу из списка активных
            delayedTasks.remove(taskId);
        }, delayTicks);
        
        // Сохраняем задачу
        delayedTasks.put(taskId, task);
        
        return taskId;
    }
    
    /**
     * Запускает повторяющееся выполнение блока.
     * 
     * @param block Блок для выполнения
     * @param player Игрок, от имени которого выполняется блок
     * @param delayTicks Начальная задержка в тиках
     * @param periodTicks Период повторения в тиках
     * @param taskId Уникальный идентификатор задачи
     * @param iterations Количество итераций (0 или меньше = бесконечно)
     * @return Идентификатор задачи
     */
    public String scheduleRepeating(Block block, Player player, long delayTicks, 
                                  long periodTicks, String taskId, int iterations) {
        if (taskId == null) {
            taskId = "repeating_" + UUID.randomUUID().toString();
        } else if (delayedTasks.containsKey(taskId) || repeatingTasks.containsKey(taskId)) {
            throw new IllegalArgumentException("Задача с таким ID уже существует: " + taskId);
        }
        
        // Создаем задачу
        Runnable taskRunnable = new Runnable() {
            private int currentIteration = 0;
            
            @Override
            public void run() {
                // Проверяем количество итераций
                if (iterations > 0 && ++currentIteration > iterations) {
                    cancelTask(taskId);
                    return;
                }
                
                // Выполняем блок
                scriptManager.executeScript("repeating_" + taskId + "_" + currentIteration, 
                                          block, player, null);
            }
        };
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
            plugin, 
            taskRunnable, 
            delayTicks, 
            periodTicks
        );
        
        // Сохраняем задачу
        repeatingTasks.put(taskId, task);
        
        return taskId;
    }
    
    /**
     * Отменяет выполнение отложенной задачи.
     */
    public boolean cancelTask(String taskId) {
        BukkitTask delayedTask = delayedTasks.remove(taskId);
        BukkitTask repeatingTask = repeatingTasks.remove(taskId);
        
        if (delayedTask != null) {
            delayedTask.cancel();
            return true;
        } else if (repeatingTask != null) {
            repeatingTask.cancel();
            return true;
        }
        
        return false;
    }
    
    /**
     * Отменяет все задачи, связанные с указанным игроком.
     */
    public void cancelAllTasks(Player player) {
        // Для отложенных задач
        delayedTasks.entrySet().removeIf(entry -> {
            // TODO: Добавить проверку на принадлежность задачи игроку
            entry.getValue().cancel();
            return true;
        });
        
        // Для повторяющихся задач
        repeatingTasks.entrySet().removeIf(entry -> {
            // TODO: Добавить проверку на принадлежность задачи игроку
            entry.getValue().cancel();
            return true;
        });
    }
    
    /**
     * Отменяет все задачи.
     */
    public void cancelAllTasks() {
        // Отменяем отложенные задачи
        delayedTasks.values().forEach(BukkitTask::cancel);
        delayedTasks.clear();
        
        // Отменяем повторяющиеся задачи
        repeatingTasks.values().forEach(BukkitTask::cancel);
        repeatingTasks.clear();
    }
    
    /**
     * Выполняет блок в следующем тике.
     */
    public void runNextTick(Block block, Player player) {
        scheduleDelayed(block, player, 1, null);
    }
    
    /**
     * Выполняет блок асинхронно.
     */
    public void runAsync(Block block, Player player) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> 
                scriptManager.executeScript("async_" + System.currentTimeMillis(), 
                                          block, player, null)
            );
        } else {
            scriptManager.executeScript("async_" + System.currentTimeMillis(), 
                                      block, player, null);
        }
    }
}
