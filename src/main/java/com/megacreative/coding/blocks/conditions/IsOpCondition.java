package com.megacreative.coding.blocks.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import com.megacreative.coding.values.BooleanValue;
import org.bukkit.entity.Player;

/**
 * Условие для проверки, является ли игрок оператором.
 * Поддерживает получение требуемого значения из параметра "required".
 */
public class IsOpCondition implements BlockCondition {
    
    // Аргумент для получения требуемого значения
    private final Argument<TextValue> requiredArgument = new ParameterArgument("required");
    
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        // Получаем требуемое значение из параметра
        TextValue requiredValue = requiredArgument.parse(context.getCurrentBlock()).orElse(null);
        
        // Если параметр не указан, проверяем просто на OP
        if (requiredValue == null) {
            return player.isOp();
        }
        
        // Если указан параметр, проверяем его значение
        String requiredStr = requiredValue.get(context);
        BooleanValue requiredBoolean = new BooleanValue(requiredStr);
        boolean required = requiredBoolean.get(context);
        
        return player.isOp() == required;
    }
} 