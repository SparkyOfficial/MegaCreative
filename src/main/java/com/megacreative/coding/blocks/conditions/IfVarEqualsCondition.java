package com.megacreative.coding.blocks.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.entity.Player;

/**
 * Условие для проверки равенства переменной заданному значению.
 * Поддерживает получение имени переменной и ожидаемого значения из параметров.
 */
public class IfVarEqualsCondition implements BlockCondition {
    
    // Аргументы для получения данных
    private final Argument<TextValue> varNameArgument = new ParameterArgument("variable");
    private final Argument<TextValue> expectedValueArgument = new ParameterArgument("value");
    
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        // 1. Получаем имя переменной
        TextValue varNameValue = varNameArgument.parse(context.getCurrentBlock()).orElse(null);
        if (varNameValue == null) {
            return false;
        }
        
        // 2. Получаем ожидаемое значение
        TextValue expectedValueValue = expectedValueArgument.parse(context.getCurrentBlock()).orElse(null);
        if (expectedValueValue == null) {
            return false;
        }
        
        try {
            // 3. Вычисляем значения
            String varName = varNameValue.get(context);
            String expectedValue = expectedValueValue.get(context);
            
            // 4. Проверяем валидность
            if (varName == null || varName.trim().isEmpty()) {
                return false;
            }
            
            // 5. Получаем значение переменной
            Object variableValue = context.getVariable(varName);
            
            if (variableValue == null) {
                return false;
            }
            
            // 6. Сравниваем значения
            return variableValue.toString().equals(expectedValue);
            
        } catch (Exception e) {
            if (player != null) {
                player.sendMessage("§cОшибка в условии 'Если переменная равна': " + e.getMessage());
            }
            return false;
        }
    }
} 