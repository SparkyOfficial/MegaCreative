package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WhileLoopAction implements BlockAction {
    
    private final Argument<TextValue> conditionArgument;
    private final Argument<TextValue> maxIterationsArgument;
    
    public WhileLoopAction() {
        this.conditionArgument = new ParameterArgument("condition");
        this.maxIterationsArgument = new ParameterArgument("maxIterations");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        try {
            TextValue maxIterationsValue = maxIterationsArgument.parse(context.getCurrentBlock()).orElse(null);
            int maxIterations = 100; // По умолчанию
            
            if (maxIterationsValue != null) {
                String maxIterationsStr = maxIterationsValue.get(context);
                maxIterations = Integer.parseInt(maxIterationsStr);
            }
            
            if (maxIterations <= 0) {
                player.sendMessage("§cОшибка: максимальное количество итераций должно быть больше 0!");
                return;
            }
            
            if (maxIterations > 1000) {
                player.sendMessage("§cПредупреждение: слишком много максимальных итераций (" + maxIterations + "). Ограничено до 1000.");
                maxIterations = 1000;
            }
            
            int iterations = 0;
            boolean conditionMet = true;
            
            // Выполняем цикл пока условие истинно
            while (conditionMet && iterations < maxIterations) {
                // Проверяем условие (пока просто проверяем переменную)
                String conditionVar = "while_condition";
                Object conditionValue = context.getVariable(conditionVar);
                
                if (conditionValue == null) {
                    // Если переменная не установлена, используем условие из параметра
                    TextValue conditionValueObj = conditionArgument.parse(context.getCurrentBlock()).orElse(null);
                    if (conditionValueObj != null) {
                        String conditionStr = conditionValueObj.get(context);
                        conditionMet = Boolean.parseBoolean(conditionStr);
                    } else {
                        conditionMet = false; // Нет условия - выходим из цикла
                    }
                } else {
                    conditionMet = Boolean.parseBoolean(conditionValue.toString());
                }
                
                if (conditionMet) {
                    // Устанавливаем переменные цикла
                    context.setVariable("while_index", iterations);
                    context.setVariable("while_iteration", iterations + 1);
                    
                    // Выполняем все дочерние блоки
                    for (var child : context.getCurrentBlock().getChildren()) {
                        Location childLocation = findBlockLocation(child);
                        ExecutionContext childContext = context.withCurrentBlock(child, childLocation);
                        context.getPlugin().getCodingManager().getScriptExecutor().processBlock(child, childContext);
                    }
                    
                    iterations++;
                }
            }
            
            if (iterations >= maxIterations) {
                player.sendMessage("§c⚠ Цикл While остановлен: достигнуто максимальное количество итераций (" + maxIterations + ")");
            } else {
                player.sendMessage("§a✓ Цикл While выполнен " + iterations + " раз");
            }
            
            context.getPlugin().getLogger().info("While loop executed " + iterations + " times for player " + player.getName());
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Цикл While': " + e.getMessage());
        }
    }
    
    private Location findBlockLocation(com.megacreative.coding.CodeBlock block) {
        // TODO: Реализовать поиск локации блока
        return null;
    }
} 