package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

public class CheckWorldWeatherCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;

        try {
            // Get parameters from the container configuration
            String weather = getWeatherFromContainer(block, context);
            if (weather == null || weather.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckWorldWeatherCondition: 'weather' parameter is missing.");
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
    
    /**
     * Gets weather from the container configuration
     */
    private String getWeatherFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get weather from the weather_slot
                Integer weatherSlot = slotResolver.apply("weather_slot");
                if (weatherSlot != null) {
                    ItemStack weatherItem = block.getConfigItem(weatherSlot);
                    if (weatherItem != null && weatherItem.hasItemMeta()) {
                        // Extract weather from item
                        return getWeatherFromItem(weatherItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting weather from container in CheckWorldWeatherCondition: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts weather from an item
     */
    private String getWeatherFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the weather
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
}