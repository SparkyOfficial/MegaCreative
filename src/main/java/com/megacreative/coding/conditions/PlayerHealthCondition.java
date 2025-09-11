package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a player's health meets specific criteria from container configuration.
 * This condition returns true if the player's health meets the specified criteria.
 */
public class PlayerHealthCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            PlayerHealthParams params = getHealthParamsFromContainer(block, context);
            
            // Parse health parameter
            double health;
            try {
                health = Double.parseDouble(params.healthStr);
            } catch (NumberFormatException e) {
                return false;
            }

            // Get optional comparison operator (default to "greater_or_equal")
            String operator = "greater_or_equal";
            if (params.operatorStr != null && !params.operatorStr.isEmpty()) {
                operator = params.operatorStr.toLowerCase();
            }

            // Check player's health against the specified value
            double playerHealth = player.getHealth();
            
            switch (operator) {
                case "equal":
                case "equals":
                    return playerHealth == health;
                case "greater":
                    return playerHealth > health;
                case "greater_or_equal":
                    return playerHealth >= health;
                case "less":
                    return playerHealth < health;
                case "less_or_equal":
                    return playerHealth <= health;
                default:
                    return playerHealth >= health; // Default to greater_or_equal
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets health parameters from the container configuration
     */
    private PlayerHealthParams getHealthParamsFromContainer(CodeBlock block, ExecutionContext context) {
        PlayerHealthParams params = new PlayerHealthParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get health from the health_slot
                Integer healthSlot = slotResolver.apply("health_slot");
                if (healthSlot != null) {
                    ItemStack healthItem = block.getConfigItem(healthSlot);
                    if (healthItem != null && healthItem.hasItemMeta()) {
                        // Extract health from item
                        params.healthStr = getHealthFromItem(healthItem);
                    }
                }
                
                // Get operator from the operator_slot
                Integer operatorSlot = slotResolver.apply("operator_slot");
                if (operatorSlot != null) {
                    ItemStack operatorItem = block.getConfigItem(operatorSlot);
                    if (operatorItem != null && operatorItem.hasItemMeta()) {
                        // Extract operator from item
                        params.operatorStr = getOperatorFromItem(operatorItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting health parameters from container in PlayerHealthCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts health from an item
     */
    private String getHealthFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the health
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
     * Helper class to hold health parameters
     */
    private static class PlayerHealthParams {
        String healthStr = "";
        String operatorStr = "";
    }
}