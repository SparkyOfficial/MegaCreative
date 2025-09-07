package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.World;

public class CheckWorldWeatherCondition implements BlockCondition {
    @Override
    public ExecutionResult evaluate(CodeBlock block, ExecutionContext context) {
        try {
            // Получаем параметр 'weather' из блока
            String requiredWeather = block.getParameter("weather").asString();
            String worldName = block.getParameter("world").asString();
            
            // Если имя мира не указано, используем мир игрока
            World world = context.getPlayer().getWorld();
            if (worldName != null && !worldName.isEmpty()) {
                world = context.getPlayer().getServer().getWorld(worldName);
                if (world == null) {
                    return ExecutionResult.failure("World not found: " + worldName);
                }
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
            
            return ExecutionResult.success(Boolean.toString(conditionMet));
            
        } catch (Exception e) {
            return ExecutionResult.failure("Error checking weather condition: " + e.getMessage());
        }
    }
}