package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.World;

/**
 * Action for setting the weather in a world.
 * This action changes the world weather based on the new parameter system.
 */
@BlockMeta(id = "setWeather", displayName = "§aSet Weather", type = BlockType.ACTION)
public class SetWeatherAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue weatherValue = block.getParameter("weather");
            
            if (weatherValue == null || weatherValue.isEmpty()) {
                return ExecutionResult.error("Weather type is not configured");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedWeather = resolver.resolve(context, weatherValue);
            
            String weatherType = resolvedWeather.asString();
            
            // Fix for Qodana issue: Condition weatherType == null is always false
            // This was a false positive - we need to properly check for empty strings
            if (weatherType.isEmpty()) {
                return ExecutionResult.error("Weather type is not configured");
            }

            
            World world = player.getWorld();
            
            switch (weatherType.toLowerCase()) {
                case "clear":
                case "sunny":
                    world.setStorm(false);
                    world.setThundering(false);
                    return ExecutionResult.success("Weather set to clear");
                    
                case "rain":
                case "storm":
                    world.setStorm(true);
                    world.setThundering(false);
                    return ExecutionResult.success("Weather set to rain");
                    
                case "thunder":
                case "thunderstorm":
                    world.setStorm(true);
                    world.setThundering(true);
                    return ExecutionResult.success("Weather set to thunderstorm");
                    
                default:
                    return ExecutionResult.error("Invalid weather type: " + weatherType);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set weather: " + e.getMessage());
        }
    }
}