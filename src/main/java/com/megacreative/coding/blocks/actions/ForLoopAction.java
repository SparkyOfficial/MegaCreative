package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ForLoopAction implements BlockAction {
    
    private final Argument<TextValue> iterationsArgument;
    
    public ForLoopAction() {
        this.iterationsArgument = new ParameterArgument("iterations");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        try {
            TextValue iterationsValue = iterationsArgument.parse(context.getCurrentBlock()).orElse(null);
            if (iterationsValue == null) {
                player.sendMessage("§cОшибка: не указано количество итераций!");
                return;
            }
            
            String iterationsStr = iterationsValue.get(context);
            int iterations = Integer.parseInt(iterationsStr);
            
            if (iterations <= 0) {
                player.sendMessage("§cОшибка: количество итераций должно быть больше 0!");
                return;
            }
            
            if (iterations > 100) {
                player.sendMessage("§cПредупреждение: слишком много итераций (" + iterations + "). Ограничено до 100.");
                iterations = 100;
            }
            
            // Выполняем дочерние блоки указанное количество раз
            for (int i = 0; i < iterations; i++) {
                // Устанавливаем переменную цикла
                context.setVariable("loop_index", i);
                context.setVariable("loop_iteration", i + 1);
                
                // Выполняем все дочерние блоки
                for (var child : context.getCurrentBlock().getChildren()) {
                    Location childLocation = findBlockLocation(child);
                    ExecutionContext childContext = context.withCurrentBlock(child, childLocation);
                    context.getPlugin().getCodingManager().getScriptExecutor().processBlock(child, childContext);
                }
            }
            
            player.sendMessage("§a✓ Цикл выполнен " + iterations + " раз");
            context.getPlugin().getLogger().info("For loop executed " + iterations + " times for player " + player.getName());
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Цикл For': " + e.getMessage());
        }
    }
    
    private Location findBlockLocation(com.megacreative.coding.CodeBlock block) {
        // TODO: Реализовать поиск локации блока
        return null;
    }
} 