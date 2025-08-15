package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.NumberParameterArgument;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import com.megacreative.coding.values.NumberValue;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * Действие для генерации случайного числа.
 * Поддерживает получение минимального и максимального значений из параметров.
 */
public class RandomNumberAction implements BlockAction {
    
    private static final Random random = new Random();
    
    // Аргументы для получения данных
    private final Argument<NumberValue> minArgument = new NumberParameterArgument("min");
    private final Argument<NumberValue> maxArgument = new NumberParameterArgument("max");
    private final Argument<TextValue> varNameArgument = new ParameterArgument("var");
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // 1. Получаем минимальное значение
        NumberValue minValue = minArgument.parse(context.getCurrentBlock()).orElse(null);
        if (minValue == null) {
            player.sendMessage("§cОшибка: не указано минимальное значение!");
            return;
        }
        
        // 2. Получаем максимальное значение
        NumberValue maxValue = maxArgument.parse(context.getCurrentBlock()).orElse(null);
        if (maxValue == null) {
            player.sendMessage("§cОшибка: не указано максимальное значение!");
            return;
        }
        
        // 3. Получаем имя переменной
        TextValue varNameValue = varNameArgument.parse(context.getCurrentBlock()).orElse(null);
        if (varNameValue == null) {
            player.sendMessage("§cОшибка: не указано имя переменной!");
            return;
        }
        
        try {
            // 4. Вычисляем значения
            int min = minValue.get(context).intValue();
            int max = maxValue.get(context).intValue();
            String varName = varNameValue.get(context);
            
            // 5. Проверяем и корректируем диапазон
            if (min > max) {
                int temp = min;
                min = max;
                max = temp;
            }
            
            // 6. Генерируем случайное число
            int randomNumber = random.nextInt(max - min + 1) + min;
            
            // 7. Сохраняем в переменную
            context.setVariable(varName, randomNumber);
            
            // 8. Уведомляем игрока
            player.sendMessage("§a🎲 Случайное число " + randomNumber + " сохранено в переменную '" + varName + "'");
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Случайное число': " + e.getMessage());
        }
    }
} 