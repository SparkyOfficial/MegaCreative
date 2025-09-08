package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncLoopAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        // Получаем и разрешаем параметры
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawIterations = block.getParameter("iterations");
        DataValue rawDelay = block.getParameter("delay");
        
        if (rawIterations == null) {
            return ExecutionResult.error("Parameter 'iterations' is missing");
        }
        
        if (rawDelay == null) {
            return ExecutionResult.error("Parameter 'delay' is missing");
        }
        
        DataValue iterationsValue = resolver.resolve(context, rawIterations);
        DataValue delayValue = resolver.resolve(context, rawDelay);
        
        String iterationsStr = iterationsValue.asString();
        String delayStr = delayValue.asString();

        try {
            int iterations = Integer.parseInt(iterationsStr);
            int delay = Integer.parseInt(delayStr);
            
            if (iterations <= 0) {
                return ExecutionResult.error("Iterations must be greater than 0");
            }
            
            if (iterations > 10000) {
                return ExecutionResult.error("Maximum iterations is 10000");
            }
            
            if (delay < 0) {
                return ExecutionResult.error("Delay cannot be negative");
            }
            
            // Получаем дочерние блоки, которые нужно выполнять в цикле
            if (block.getChildren().isEmpty()) {
                return ExecutionResult.error("No child blocks to execute in loop");
            }
            
            // Получаем ScriptEngine из ServiceRegistry
            ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
            if (scriptEngine == null) {
                return ExecutionResult.error("Failed to get ScriptEngine");
            }
            
            // Создаем счетчик итераций
            AtomicInteger currentIteration = new AtomicInteger(0);
            
            // Запускаем BukkitRunnable для асинхронного выполнения
            Bukkit.getScheduler().runTaskAsynchronously(context.getPlugin(), () -> {
                try {
                    while (currentIteration.get() < iterations && !context.isCancelled()) {
                        // Проверяем, не отменили ли выполнение
                        if (context.isCancelled()) {
                            break;
                        }
                        
                        // Добавляем задержку между итерациями
                        if (delay > 0) {
                            try {
                                Thread.sleep(delay * 50L); // Преобразуем тики в миллисекунды
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                        
                        // Выполняем все дочерние блоки
                        for (CodeBlock childBlock : block.getChildren()) {
                            if (context.isCancelled()) {
                                break;
                            }
                            
                            // Создаем новый контекст для каждой итерации
                            ExecutionContext loopContext = context.withCurrentBlock(childBlock, context.getBlockLocation());
                            
                            // Добавляем переменные цикла
                            loopContext.setVariable("loopIndex", currentIteration.get() + 1);
                            loopContext.setVariable("loopCount", iterations);
                            
                            try {
                                // Выполняем блок синхронно в основном потоке
                                scriptEngine.executeBlockChain(childBlock, player, "async_loop")
                                    .exceptionally(throwable -> {
                                        player.sendMessage("§cОшибка в итерации " + (currentIteration.get() + 1) + ": " + throwable.getMessage());
                                        return null;
                                    })
                                    .join(); // Ждем завершения выполнения блока
                            } catch (Exception e) {
                                player.sendMessage("§cОшибка в итерации " + (currentIteration.get() + 1) + ": " + e.getMessage());
                                break;
                            }
                        }
                        
                        // Увеличиваем счетчик итераций
                        currentIteration.incrementAndGet();
                    }
                    
                    // Сообщаем игроку о завершении цикла
                    Bukkit.getScheduler().runTask(context.getPlugin(), () -> {
                        player.sendMessage("§a🔄 Асинхронный цикл выполнен " + currentIteration.get() + " раз");
                    });
                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(context.getPlugin(), () -> {
                        player.sendMessage("§cОшибка при выполнении асинхронного цикла: " + e.getMessage());
                    });
                }
            });
            
            return ExecutionResult.success("Async loop action started");
        } catch (NumberFormatException e) {
            return ExecutionResult.error("Invalid parameters: " + e.getMessage());
        }
    }
}