package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

public class RepeatAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawTimes = block.getParameter("times");
        if (rawTimes == null) {
            player.sendMessage("§cОшибка: параметр 'times' не указан");
            return;
        }
        
        DataValue timesValue = resolver.resolve(context, rawTimes);
        String timesStr = timesValue.asString();

        try {
            int times = Integer.parseInt(timesStr);
            
            if (times <= 0) {
                player.sendMessage("§cОшибка: количество повторений должно быть больше 0");
                return;
            }
            
            if (times > 1000) {
                player.sendMessage("§cОшибка: максимальное количество повторений - 1000");
                return;
            }
            
            // Получаем следующий блок для выполнения
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock == null) {
                player.sendMessage("§cОшибка: нет блока для повторения");
                return;
            }
            
            // Получаем ScriptEngine из ServiceRegistry
            ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
            if (scriptEngine == null) {
                player.sendMessage("§cОшибка: не удалось получить ScriptEngine");
                return;
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
                            scriptEngine.executeScript(nextBlock, player, "repeat_loop")
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
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметре times: " + timesStr);
        }
    }
} 