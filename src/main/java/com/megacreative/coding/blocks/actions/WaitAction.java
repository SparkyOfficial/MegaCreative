package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.NumberParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.NumberValue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Действие для ожидания указанного количества тиков.
 * Поддерживает получение количества тиков из параметра "ticks".
 */
public class WaitAction implements BlockAction {
    
    // Аргумент для получения количества тиков
    private final Argument<NumberValue> ticksArgument = new NumberParameterArgument("ticks");
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // 1. Получаем количество тиков
        NumberValue ticksValue = ticksArgument.parse(context.getCurrentBlock()).orElse(null);
        if (ticksValue == null) {
            player.sendMessage("§cОшибка: не указано количество тиков!");
            return;
        }
        
        try {
            // 2. Вычисляем количество тиков
            int ticks = ticksValue.get(context).intValue();
            
            // 3. Проверяем валидность
            if (ticks < 0) {
                player.sendMessage("§cОшибка: количество тиков не может быть отрицательным!");
                return;
            }
            
            // 4. Запускаем выполнение следующего блока через указанное количество тиков
            Bukkit.getScheduler().runTaskLater(context.getPlugin(), () -> {
                CodeBlock nextBlock = context.getCurrentBlock().getNextBlock();
                if (nextBlock != null) {
                    var executor = context.getPlugin().getCodingManager().getScriptExecutor();
                    ExecutionContext newContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
                    executor.processBlock(nextBlock, newContext);
                }
            }, ticks);
            
            // 5. Уведомляем игрока
            player.sendMessage("§a⏱ Ожидание " + ticks + " тиков...");
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Ожидание': " + e.getMessage());
        }
    }
} 