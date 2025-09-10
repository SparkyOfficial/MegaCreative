package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking the world time from container configuration.
 * This condition returns true if the world time meets the specified criteria.
 */
public class WorldTimeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null || player.getWorld() == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            WorldTimeParams params = getTimeParamsFromContainer(block, context);
            
            if (params.timeStr == null || params.timeStr.isEmpty()) {
                return false;
            }

            // Get optional comparison operator (default to "equal")
            String operator = "equal";
            if (params.operator != null && !params.operator.isEmpty()) {
                operator = params.operator.toLowerCase();
            }

            // Resolve any placeholders in the time value
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedTimeStr = resolver.resolveString(context, params.timeStr);
            
            // Parse time parameter
            long time;
            try {
                time = Long.parseLong(resolvedTimeStr);
            } catch (NumberFormatException e) {
                return false;
            }

            // Check world time against the specified value
            long worldTime = player.getWorld().getTime();
            
            switch (operator) {
                case "equal":
                case "equals":
                    return worldTime == time;
                case "greater":
                    return worldTime > time;
                case "greater_or_equal":
                    return worldTime >= time;
                case "less":
                    return worldTime < time;
                case "less_or_equal":
                    return worldTime <= time;
                default:
                    return worldTime == time; // Default to equal
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets time parameters from the container configuration
     */
    private WorldTimeParams getTimeParamsFromContainer(CodeBlock block, ExecutionContext context) {
        WorldTimeParams params = new WorldTimeParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get time from the time slot
                Integer timeSlot = slotResolver.apply("time");
                if (timeSlot != null) {
                    ItemStack timeItem = block.getConfigItem(timeSlot);
                    if (timeItem != null && timeItem.hasItemMeta()) {
                        // Extract time from item
                        params.timeStr = getTimeFromItem(timeItem);
                    }
                }
                
                // Get operator from the operator slot
                Integer operatorSlot = slotResolver.apply("operator");
                if (operatorSlot != null) {
                    ItemStack operatorItem = block.getConfigItem(operatorSlot);
                    if (operatorItem != null && operatorItem.hasItemMeta()) {
                        // Extract operator from item
                        params.operator = getOperatorFromItem(operatorItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting time parameters from container in WorldTimeCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts time from an item
     */
    private String getTimeFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the time
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts operator from an item
     */
    private String getOperatorFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the operator
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Helper class to hold time parameters
     */
    private static class WorldTimeParams {
        String timeStr = "";
        String operator = "";
    }
}