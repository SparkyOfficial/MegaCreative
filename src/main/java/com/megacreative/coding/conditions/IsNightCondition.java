package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

/**
 * Condition for checking if it's night time in the player's world from container configuration.
 * This condition returns true if it's night time in the player's world.
 */
@BlockMeta(id = "isNight", displayName = "§aIs Night", type = BlockType.CONDITION)
public class IsNightCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null || player.getWorld() == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            IsNightParams params = getTimeParamsFromContainer(block, context);
            
            // If a specific time item is provided, use it
            if (params.timeStr != null && !params.timeStr.isEmpty()) {
                // Check if the item represents night time
                // BLACK_WOOL = night, YELLOW_WOOL = day
                if ("BLACK_WOOL".equalsIgnoreCase(params.timeStr) || 
                    "BLACK_WOOL".equalsIgnoreCase(params.timeStr.replace("minecraft:", ""))) {
                    return true;
                } else if ("YELLOW_WOOL".equalsIgnoreCase(params.timeStr) || 
                           "YELLOW_WOOL".equalsIgnoreCase(params.timeStr.replace("minecraft:", ""))) {
                    return false;
                }
                // If it's not a recognized time item, fall back to checking current world time
            }
            
            // If no time is specified or not a recognized time item, check if it's currently night in the player's world
            long worldTime = player.getWorld().getTime();
            // Night time in Minecraft is from 12542 to 23459 ticks
            return worldTime >= 12542 && worldTime <= 23459;
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets time parameters from the container configuration
     */
    private IsNightParams getTimeParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IsNightParams params = new IsNightParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get time from the time_slot
                Integer timeSlot = slotResolver.apply("time_slot");
                if (timeSlot != null) {
                    ItemStack timeItem = block.getConfigItem(timeSlot);
                    if (timeItem != null) {
                        // Extract time type from item
                        params.timeStr = getTimeTypeFromItem(timeItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting time parameters from container in IsNightCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts time type from an item
     */
    private String getTimeTypeFromItem(ItemStack item) {
        // For time type, we'll use the item type name
        return item.getType().name();
    }
    
    /**
     * Helper class to hold time parameters
     */
    private static class IsNightParams {
        String timeStr = "";
    }
}