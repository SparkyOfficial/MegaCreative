package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for setting the weather in a world.
 * This action changes the world weather based on the container configuration.
 */
public class SetWeatherAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the weather type from the container configuration
            String weatherType = getWeatherTypeFromContainer(block, context);
            
            if (weatherType == null || weatherType.isEmpty()) {
                return ExecutionResult.error("Weather type is not configured");
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
    
    /**
     * Gets weather type from the container configuration
     */
    private String getWeatherTypeFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get weather from the weather slot
                Integer weatherSlot = slotResolver.apply("weather_slot");
                if (weatherSlot != null) {
                    ItemStack weatherItem = block.getConfigItem(weatherSlot);
                    if (weatherItem != null && weatherItem.hasItemMeta()) {
                        // Extract weather type from item
                        return getWeatherTypeFromItem(weatherItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting weather type from container in SetWeatherAction: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts weather type from an item
     */
    private String getWeatherTypeFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the weather type
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
}