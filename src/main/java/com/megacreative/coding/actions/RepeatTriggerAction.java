package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.HybridScriptExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RepeatTriggerAction implements BlockAction {
    private static final Map<UUID, Integer> activeTasks = new HashMap<>();
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawTicks = block.getParameter("ticks");
        Object rawAction = block.getParameter("action");

        String ticksStr = ParameterResolver.resolve(context, rawTicks);
        String actionStr = ParameterResolver.resolve(context, rawAction);

        if (ticksStr == null || actionStr == null) return;

        try {
            int ticks = Integer.parseInt(ticksStr);
            
            // Останавливаем предыдущую задачу для этого игрока, если она существует
            stopRepeatingTask(player.getUniqueId());
            
            // Создаем уникальный идентификатор для задачи
            UUID taskId = UUID.randomUUID();
            
            // Запускаем повторяющуюся задачу
            int taskIdBukkit = Bukkit.getScheduler().runTaskTimer(context.getPlugin(), () -> {
                try {
                    // Выполняем действие
                    CodeBlock nextBlock = block.getNextBlock();
                    if (nextBlock != null) {
                        HybridScriptExecutor executor = context.getPlugin().getCodingManager().getScriptExecutor();
                        ExecutionContext newContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
                        executor.processBlock(nextBlock, newContext);
                    }
                } catch (Exception e) {
                    player.sendMessage("§cОшибка в повторяющемся триггере: " + e.getMessage());
                    stopRepeatingTask(player.getUniqueId());
                }
            }, ticks, ticks).getTaskId();
            
            // Сохраняем ID задачи
            activeTasks.put(player.getUniqueId(), taskIdBukkit);
            
            player.sendMessage("§a🔄 Повторяющийся триггер запущен каждые " + ticks + " тиков");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметре ticks: " + ticksStr);
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