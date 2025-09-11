package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

public class CheckPlayerStatsCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        try {
            // Get parameters from the container configuration
            CheckPlayerStatsParams params = getStatsParamsFromContainer(block, context);
            
            if (params.statType == null || params.statType.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckPlayerStatsCondition: 'stat_type' parameter is missing.");
                return false;
            }
            
            if (params.valueStr == null || params.valueStr.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckPlayerStatsCondition: 'value' parameter is missing.");
                return false;
            }
            
            String statType = params.statType.toUpperCase();
            double checkValue = params.value;
            String operator = params.operator;
            
            // Get the player's statistic value
            double playerStatValue = 0;
            try {
                Statistic statistic = Statistic.valueOf(statType);
                playerStatValue = player.getStatistic(statistic);
            } catch (IllegalArgumentException e) {
                context.getPlugin().getLogger().warning("CheckPlayerStatsCondition: Invalid statistic type '" + statType + "'.");
                return false;
            }
            
            // Compare values based on operator
            switch (operator) {
                case ">=":
                    return playerStatValue >= checkValue;
                case "<=":
                    return playerStatValue <= checkValue;
                case ">":
                    return playerStatValue > checkValue;
                case "<":
                    return playerStatValue < checkValue;
                case "==":
                case "=":
                    return playerStatValue == checkValue;
                case "!=":
                    return playerStatValue != checkValue;
                default:
                    context.getPlugin().getLogger().warning("CheckPlayerStatsCondition: Invalid operator '" + operator + "'.");
                    return false;
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating CheckPlayerStatsCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets stats parameters from the container configuration
     */
    private CheckPlayerStatsParams getStatsParamsFromContainer(CodeBlock block, ExecutionContext context) {
        CheckPlayerStatsParams params = new CheckPlayerStatsParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get stat type from the stat_type_slot
                Integer statTypeSlot = slotResolver.apply("stat_type_slot");
                if (statTypeSlot != null) {
                    ItemStack statTypeItem = block.getConfigItem(statTypeSlot);
                    if (statTypeItem != null && statTypeItem.hasItemMeta()) {
                        // Extract stat type from item
                        params.statType = getStatTypeFromItem(statTypeItem);
                    }
                }
                
                // Get value from the value_slot
                Integer valueSlot = slotResolver.apply("value_slot");
                if (valueSlot != null) {
                    ItemStack valueItem = block.getConfigItem(valueSlot);
                    if (valueItem != null && valueItem.hasItemMeta()) {
                        // Extract value from item
                        params.valueStr = getValueFromItem(valueItem);
                        if (params.valueStr != null && !params.valueStr.isEmpty()) {
                            try {
                                params.value = Double.parseDouble(params.valueStr);
                            } catch (NumberFormatException e) {
                                // Use default value if parsing fails
                            }
                        }
                    }
                }
                
                // Get operator from the operator_slot
                Integer operatorSlot = slotResolver.apply("operator_slot");
                if (operatorSlot != null) {
                    ItemStack operatorItem = block.getConfigItem(operatorSlot);
                    if (operatorItem != null && operatorItem.hasItemMeta()) {
                        // Extract operator from item
                        params.operator = getOperatorFromItem(operatorItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting stats parameters from container in CheckPlayerStatsCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts stat type from an item
     */
    private String getStatTypeFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the stat type
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts value from an item
     */
    private String getValueFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the value
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
        return "=="; // Default operator
    }
    
    /**
     * Helper class to hold stats parameters
     */
    private static class CheckPlayerStatsParams {
        String statType = "";
        String valueStr = "";
        double value = 0.0;
        String operator = "==";
    }
}