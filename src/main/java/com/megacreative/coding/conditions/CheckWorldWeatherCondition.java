package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.World;
import org.bukkit.entity.Player;

// Шаблон для нового УСЛОВИЯ
public class CheckWorldWeatherCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        try {
            String requiredWeather = block.getParameter("weather").asString().toLowerCase();
            World world = player.getWorld(); // TODO: Добавить обработку параметра world из блока
            
            switch (requiredWeather) {
                case "clear":
                    return !world.hasStorm() && !world.isThundering();
                case "rain":
                    return world.hasStorm() && !world.isThundering();
                case "thunder":
                    return world.isThundering();
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}