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

import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

/**
 * Condition for checking world time from container configuration.
 * This condition returns true if the world time matches the specified criteria.
 */
@BlockMeta(id = "worldTime", displayName = "§aWorld Time", type = BlockType.CONDITION)
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
            
            // Parse time parameter
            long worldTime = 0;
            if (params.timeStr != null && !params.timeStr.isEmpty()) {
                try {
                    worldTime = Long.parseLong(params.timeStr);
                } catch (NumberFormatException e) {
                    // Use default time if parsing fails
                    worldTime = 0;
                }
            }

            // Resolve any placeholders in the time
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue timeValue = DataValue.of(String.valueOf(worldTime));
            DataValue resolvedTime = resolver.resolve(context, timeValue);
            
            long time;
            try {
                time = Long.parseLong(resolvedTime.asString());
            } catch (NumberFormatException e) {
                // Use default time if parsing fails
                time = 0;
            }

            // Parse operator parameter (default to "equal")
            String operator = params.operatorStr != null && !params.operatorStr.isEmpty() ? params.operatorStr : "equal";

            // Get the current world time
            long currentWorldTime = player.getWorld().getTime();

            // Compare based on operator
            switch (operator.toLowerCase()) {
                case "equal":
                case "equals":
                case "==":
                    return currentWorldTime == time;
                case "greater":
                case "greater_than":
                case ">":
                    return currentWorldTime > time;
                case "less":
                case "less_than":
                case "<":
                    return currentWorldTime < time;
                case "greater_or_equal":
                case ">=":
                    return currentWorldTime >= time;
                case "less_or_equal":
                case "<=":
                    return currentWorldTime <= time;
                default:
                    return currentWorldTime == time;
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
                        params.operatorStr = getOperatorFromItem(operatorItem);
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
        String operatorStr = "";
    }
}