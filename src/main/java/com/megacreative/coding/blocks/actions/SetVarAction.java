package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.entity.Player;

/**
 * Действие для установки переменной.
 * Поддерживает получение имени переменной и значения из параметров.
 */
public class SetVarAction implements BlockAction {
    
    // Аргументы для получения данных
    private final Argument<TextValue> varNameArgument = new ParameterArgument("var");
    private final Argument<TextValue> valueArgument = new ParameterArgument("value");
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // 1. Получаем имя переменной
        TextValue varNameValue = varNameArgument.parse(context.getCurrentBlock()).orElse(null);
        if (varNameValue == null) {
            player.sendMessage("§cОшибка: не указано имя переменной!");
            return;
        }
        
        // 2. Получаем значение
        TextValue valueValue = valueArgument.parse(context.getCurrentBlock()).orElse(null);
        if (valueValue == null) {
            player.sendMessage("§cОшибка: не указано значение!");
            return;
        }
        
        try {
            // 3. Вычисляем значения
            String varName = varNameValue.get(context);
            String value = valueValue.get(context);
            
            // 4. Проверяем имя переменной
            if (varName == null || varName.trim().isEmpty()) {
                player.sendMessage("§cОшибка: имя переменной не может быть пустым!");
                return;
            }
            
            // 5. Устанавливаем переменную
            context.setVariable(varName, value);
            
            // 6. Уведомляем игрока
            player.sendMessage("§a✓ Переменная '" + varName + "' установлена в: " + value);
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Установить переменную': " + e.getMessage());
        }
    }
} 