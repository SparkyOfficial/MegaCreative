package com.megacreative.coding.blocks.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.NumberParameterArgument;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import com.megacreative.coding.values.NumberValue;
import org.bukkit.entity.Player;

/**
 * Условие для проверки здоровья игрока.
 * Поддерживает получение требуемого здоровья и оператора сравнения из параметров.
 */
public class PlayerHealthCondition implements BlockCondition {
    
    // Аргументы для получения данных
    private final Argument<NumberValue> healthArgument = new NumberParameterArgument("health");
    private final Argument<TextValue> operatorArgument = new ParameterArgument("operator");
    
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        // 1. Получаем требуемое здоровье
        NumberValue healthValue = healthArgument.parse(context.getCurrentBlock()).orElse(null);
        if (healthValue == null) {
            return false;
        }
        
        // 2. Получаем оператор сравнения
        TextValue operatorValue = operatorArgument.parse(context.getCurrentBlock()).orElse(null);
        if (operatorValue == null) {
            return false;
        }
        
        try {
            // 3. Вычисляем значения
            double requiredHealth = healthValue.get(context);
            String operator = operatorValue.get(context);
            double currentHealth = player.getHealth();
            
            // 4. Выполняем сравнение
            switch (operator) {
                case ">":
                    return currentHealth > requiredHealth;
                case ">=":
                    return currentHealth >= requiredHealth;
                case "<":
                    return currentHealth < requiredHealth;
                case "<=":
                    return currentHealth <= requiredHealth;
                case "==":
                case "=":
                    return currentHealth == requiredHealth;
                default:
                    return false;
            }
            
        } catch (Exception e) {
            if (player != null) {
                player.sendMessage("§cОшибка в условии 'Здоровье игрока': " + e.getMessage());
            }
            return false;
        }
    }
} 