package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.World;

public class CheckWorldWeatherCondition implements BlockCondition {
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        try {
            // Получаем параметр 'weather' из блока
            DataValue weatherValue = block.getParameter("weather");
            String requiredWeather = weatherValue != null ? weatherValue.asString() : "clear";
            
            DataValue worldValue = block.getParameter("world");
            String worldName = worldValue != null ? worldValue.asString() : null;
            
            // Если имя мира не указано, используем мир игрока
            World world = context.getPlayer() != null ? context.getPlayer().getWorld() : null;
            if (worldName != null && !worldName.isEmpty()) {
                world = context.getPlugin().getServer().getWorld(worldName);
                if (world == null) {
                    context.getPlugin().getLogger().severe("World not found: " + worldName);
                    return false;
                }
            } else if (world == null) {
                context.getPlugin().getLogger().severe("No world available for weather check");
                return false;
            }
            
            // Проверяем погоду в мире
            boolean isRaining = world.hasStorm();
            boolean isThundering = world.isThundering();
            
            boolean conditionMet = false;
            if ("clear".equalsIgnoreCase(requiredWeather)) {
                conditionMet = !isRaining && !isThundering;
            } else if ("rain".equalsIgnoreCase(requiredWeather)) {
                conditionMet = isRaining && !isThundering;
            } else if ("thunder".equalsIgnoreCase(requiredWeather)) {
                conditionMet = isThundering;
            }
            
            return conditionMet;
            
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error checking weather condition: " + e.getMessage());
            return false;
        }
    }
}