package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SetWeatherAction implements BlockAction {
    
    private final Argument<TextValue> weatherArgument;
    
    public SetWeatherAction() {
        this.weatherArgument = new ParameterArgument("weather");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        try {
            String weatherStr = weatherArgument.parse(context.getCurrentBlock()).get().get(context);
            
            Player player = context.getPlayer();
            World world = player.getWorld();
            
            switch (weatherStr.toLowerCase()) {
                case "clear":
                case "sunny":
                    world.setStorm(false);
                    world.setThundering(false);
                    break;
                case "rain":
                case "storm":
                    world.setStorm(true);
                    world.setThundering(false);
                    break;
                case "thunder":
                case "thunderstorm":
                    world.setStorm(true);
                    world.setThundering(true);
                    break;
                default:
                    context.getPlugin().getLogger().warning("Unknown weather type: " + weatherStr);
                    return;
            }
            
            context.getPlugin().getLogger().info("Set weather to " + weatherStr + " in world " + world.getName());
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Failed to set weather: " + e.getMessage());
        }
    }
} 