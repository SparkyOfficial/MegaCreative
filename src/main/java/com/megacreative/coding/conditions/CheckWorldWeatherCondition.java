package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.World;
import org.bukkit.entity.Player;

@BlockMeta(id = "checkWorldWeather", displayName = "§aCheck World Weather", type = BlockType.CONDITION)
public class CheckWorldWeatherCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;

        try {
            
            DataValue weatherValue = block.getParameter("weather");
            if (weatherValue == null || weatherValue.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckWorldWeatherCondition: 'weather' parameter is missing.");
                return false;
            }
            
            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedWeather = resolver.resolve(context, weatherValue);
            
            String weather = resolvedWeather.asString();
            // Fix for Qodana issue: Condition weather == null is always false
            // This was a false positive - we need to properly check for empty strings
            if (weather.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckWorldWeatherCondition: 'weather' parameter is empty.");
                return false;
            }

            World world = player.getWorld();

            switch (weather.toLowerCase()) {
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