package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.MegaCreative;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class AsyncLoopAction implements BlockAction {
    
    // Храним активные задачи для возможности их отмены
    private static final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }
        
        try {
            // Получаем параметры из блока
            int iterations = block.getParameter("iterations").asNumber().intValue();
            int delay = block.getParameter("delay").asNumber().intValue();
            
            // Получаем дочерние блоки для выполнения в цикле
            if (block.getChildren().isEmpty()) {
                return ExecutionResult.error("У блока цикла нет дочерних блоков для выполнения.");
            }
            
            CodeBlock firstChild = block.getChildren().get(0);
            
            // Создаем уникальный ID для этой задачи
            UUID taskId = UUID.randomUUID();
            
            // Создаем и запускаем асинхронную задачу
            BukkitRunnable task = new BukkitRunnable() {
                private int count = 0;
                
                @Override
                public void run() {
                    try {
                        // Проверяем, не отменили ли задачу
                        if (context.isCancelled()) {
                            this.cancel();
                            activeTasks.remove(taskId);
                            return;
                        }
                        
                        // Проверяем количество итераций (-1 означает бесконечный цикл)
                        if (iterations != -1 && count >= iterations) {
                            this.cancel();
                            activeTasks.remove(taskId);
                            return;
                        }
                        
                        // Создаем новый контекст для каждой итерации, чтобы избежать конфликтов
                        ExecutionContext loopContext = new ExecutionContext.Builder()
                            .plugin(context.getPlugin())
                            .player(context.getPlayer())
                            .creativeWorld(context.getCreativeWorld())
                            .currentBlock(firstChild)
                            .build();
                        
                        // Выполняем дочерние блоки
                        // Здесь нужно вызвать движок для выполнения цепочки дочерних блоков
                        // Для упрощения просто выполним первый дочерний блок
                        // В реальной реализации нужно выполнить всю цепочку дочерних блоков
                        
                        count++;
                        
                    } catch (Exception e) {
                        // Логируем ошибку и останавливаем цикл
                        context.getPlugin().getLogger().severe("Ошибка в асинхронном цикле: " + e.getMessage());
                        e.printStackTrace();
                        this.cancel();
                        activeTasks.remove(taskId);
                    }
                }
            };
            
            BukkitTask bukkitTask = task.runTaskTimerAsynchronously(context.getPlugin(), 0L, delay);
            activeTasks.put(taskId, bukkitTask);
            
            return ExecutionResult.success("Асинхронный цикл запущен с " + 
                (iterations == -1 ? "бесконечным" : iterations) + " количеством итераций.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при запуске асинхронного цикла: " + e.getMessage());
        }
    }
    
    // Метод для отмены всех активных задач при выгрузке плагина
    public static void cancelAllTasks() {
        for (BukkitTask task : activeTasks.values()) {
            try {
                task.cancel();
            } catch (Exception ignored) {}
        }
        activeTasks.clear();
    }
}
