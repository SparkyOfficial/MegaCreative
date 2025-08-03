package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class RepeatAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawTimes = block.getParameter("times");
        String timesStr = ParameterResolver.resolve(context, rawTimes);

        if (timesStr == null) {
            player.sendMessage("§cОшибка: параметр 'times' не указан");
            return;
        }

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
            
            // Получаем дочерние блоки для выполнения цикла
            List<CodeBlock> children = block.getChildren();
            
            if (children.isEmpty()) {
                // Если дочерних блоков нет, просто идем дальше
                player.sendMessage("§e⚠ Нет блоков для повторения");
            } else {
                // Выполняем цикл с дочерними блоками
                var executor = context.getPlugin().getCodingManager().getScriptExecutor();
                
                for (int i = 0; i < times; i++) {
                    // Добавляем переменные цикла
                    context.setVariable("loopIndex", i + 1);
                    context.setVariable("loopCount", times);
                    
                    // Выполняем каждый дочерний блок
                    for (CodeBlock child : children) {
                        try {
                            Location childLocation = executor.findBlockLocation(child);
                            ExecutionContext childContext = context.withCurrentBlock(child, childLocation);
                            executor.processBlock(child, childContext);
                        } catch (Exception e) {
                            player.sendMessage("§cОшибка в итерации " + (i + 1) + ": " + e.getMessage());
                            break;
                        }
                    }
                }
            }
            
            player.sendMessage("§a🔄 Цикл выполнен " + times + " раз");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметре times: " + timesStr);
        }
    }
} 