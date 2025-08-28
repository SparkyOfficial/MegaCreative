package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptExecutor;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class RepeatAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
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
            
            // Выполняем блок указанное количество раз
            ScriptExecutor executor = new ScriptExecutor(context.getPlugin());
            
            for (int i = 0; i < times; i++) {
                // Создаем новый контекст для каждой итерации
                ExecutionContext loopContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
                
                // Добавляем переменную с номером итерации
                loopContext.setVariable("loopIndex", i + 1);
                loopContext.setVariable("loopCount", times);
                
                try {
                    executor.processBlock(nextBlock, loopContext);
                } catch (Exception e) {
                    player.sendMessage("§cОшибка в итерации " + (i + 1) + ": " + e.getMessage());
                    break;
                }
            }
            
            player.sendMessage("§a🔄 Цикл выполнен " + times + " раз");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметре times: " + timesStr);
        }
    }
} 