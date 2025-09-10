package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.World;

/**
 * Action for setting the weather in a world.
 * This action changes the world weather based on the parameter.
 */
public class SetWeatherAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the weather parameter from the block
            DataValue weatherValue = block.getParameter("weather");
            if (weatherValue == null) {
                return ExecutionResult.error("Weather parameter is missing");
            }

            // Resolve any placeholders in the weather type
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedWeather = resolver.resolve(context, weatherValue);
            
            // Parse weather parameter
            String weatherType = resolvedWeather.asString();
            if (weatherType == null || weatherType.isEmpty()) {
                return ExecutionResult.error("Weather type is empty or null");
            }

            // Set the weather in the world
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