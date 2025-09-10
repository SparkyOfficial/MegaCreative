package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CheckWorldWeatherCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;

        try {
            // Get parameters
            DataValue weatherValue = block.getParameter("weather");
            if (weatherValue == null || weatherValue.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckWorldWeatherCondition: 'weather' parameter is missing.");
                return false;
            }

            String weather = weatherValue.asString().toLowerCase();
            World world = player.getWorld();

            switch (weather) {
                case "clear":
                    return !world.hasStorm() && !world.isThundering();
                case "rain":
                    return world.hasStorm() && !world.isThundering();
                case "thunder":
                    return world.hasStorm() && world.isThundering();
                default:
                    context.getPlugin().getLogger().warning("CheckWorldWeatherCondition: Invalid weather type '" + weather + "'.");
                    return false;
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating CheckWorldWeatherCondition: " + e.getMessage());
            return false;
        }
    }
}