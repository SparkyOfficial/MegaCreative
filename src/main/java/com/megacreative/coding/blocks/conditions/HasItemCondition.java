package com.megacreative.coding.blocks.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Условие для проверки наличия предмета у игрока.
 * Поддерживает получение названия предмета из параметра "item".
 */
public class HasItemCondition implements BlockCondition {
    
    // Аргумент для получения названия предмета
    private final Argument<TextValue> itemNameArgument = new ParameterArgument("item");
    
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        // Получаем название предмета из параметра
        TextValue itemNameValue = itemNameArgument.parse(context.getCurrentBlock()).orElse(null);
        if (itemNameValue == null) {
            return false;
        }
        
        try {
            // Вычисляем название предмета (обрабатываем плейсхолдеры)
            String itemName = itemNameValue.get(context);
            Material material = Material.valueOf(itemName.toUpperCase());
            
            // Проверяем наличие предмета в инвентаре
            return player.getInventory().contains(material);
            
        } catch (IllegalArgumentException e) {
            // Если неверный материал, возвращаем false
            return false;
        }
    }
} 