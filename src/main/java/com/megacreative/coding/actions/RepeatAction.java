package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

public class RepeatAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        // Получаем и разрешаем параметры
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawTimes = block.getParameter("times");
        if (rawTimes == null) {
            return ExecutionResult.error("Parameter 'times' is missing");
        }
        
        DataValue timesValue = resolver.resolve(context, rawTimes);
        String timesStr = timesValue.asString();

        try {
            int times = Integer.parseInt(timesStr);
            
            if (times <= 0) {
                return ExecutionResult.error("Repeat count must be greater than 0");
            }
            
            if (times > 1000) {
                return ExecutionResult.error("Maximum repeat count is 1000");
            }
            
            // Получаем следующий блок для выполнения
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock == null) {
                return ExecutionResult.error("No block to repeat");
            }
            
            // Получаем ScriptEngine из ServiceRegistry
            ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
            if (scriptEngine == null) {
                return ExecutionResult.error("Failed to get ScriptEngine");
            }
            
            // Запускаем выполнение в асинхронном контексте
            CompletableFuture.runAsync(() -> {
                try {
                    for (int i = 0; i < times; i++) {
                        // Создаем новый контекст для каждой итерации
                        ExecutionContext loopContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
                        
                        // Добавляем переменную с номером итерации
                        loopContext.setVariable("loopIndex", i + 1);
                        loopContext.setVariable("loopCount", times);
                        
                        // Выполняем блок синхронно в основном потоке
                        try {
                            scriptEngine.executeBlockChain(nextBlock, player, "repeat_loop")
                                .exceptionally(throwable -> {
                                    player.sendMessage("§cОшибка в итерации " + (i + 1) + ": " + throwable.getMessage());
                                    return null;
                                })
                                .join(); // Ждем завершения итерации
                        } catch (Exception e) {
                            player.sendMessage("§cОшибка в итерации " + (i + 1) + ": " + e.getMessage());
                            break;
                        }
                    }
                    
                    player.sendMessage("§a🔄 Цикл выполнен " + times + " раз");
                } catch (Exception e) {
                    player.sendMessage("§cОшибка при выполнении цикла: " + e.getMessage());
                }
            });
            
            return ExecutionResult.success("Repeat action started");
        } catch (NumberFormatException e) {
            return ExecutionResult.error("Invalid times parameter: " + timesStr);
        }
    }
}