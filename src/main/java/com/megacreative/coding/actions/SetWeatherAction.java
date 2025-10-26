package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action to set the weather in a world
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "setWeather", displayName = "§bSet Weather", type = BlockType.ACTION)
public class SetWeatherAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameter
            DataValue weatherValue = block.getParameter("weather");
            
            if (weatherValue == null) {
                return ExecutionResult.error("Missing required parameter: weather");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedWeather = resolver.resolve(context, weatherValue);
            
            String weatherStr = resolvedWeather.asString();
            
            // Set weather based on parameter
            switch (weatherStr.toLowerCase()) {
                case "clear":
                    player.getWorld().setStorm(false);
                    player.getWorld().setThundering(false);
                    break;
                case "rain":
                    player.getWorld().setStorm(true);
                    player.getWorld().setThundering(false);
                    break;
                case "thunder":
                    player.getWorld().setStorm(true);
                    player.getWorld().setThundering(true);
                    break;
                default:
                    return ExecutionResult.error("Invalid weather type: " + weatherStr);
            }
            
            return ExecutionResult.success("Set weather to " + weatherStr);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set weather: " + e.getMessage());
        }
    }
}