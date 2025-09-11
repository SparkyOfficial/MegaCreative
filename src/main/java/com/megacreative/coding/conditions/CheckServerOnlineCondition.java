package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

public class CheckServerOnlineCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters from the container configuration
            String checkType = getCheckTypeFromContainer(block, context);

            int playerCount = Bukkit.getOnlinePlayers().size();
            
            switch (checkType.toLowerCase()) {
                case "online":
                    return playerCount > 0;
                case "empty":
                    return playerCount == 0;
                case "full":
                    return Bukkit.getMaxPlayers() <= playerCount;
                default:
                    // Check if player count is greater than or equal to a specific number
                    try {
                        int requiredPlayers = Integer.parseInt(checkType);
                        return playerCount >= requiredPlayers;
                    } catch (NumberFormatException e) {
                        context.getPlugin().getLogger().warning("CheckServerOnlineCondition: Invalid check_type '" + checkType + "'.");
                        return false;
                    }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating CheckServerOnlineCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets check type from the container configuration
     */
    private String getCheckTypeFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get check type from the check_type_slot
                Integer checkTypeSlot = slotResolver.apply("check_type_slot");
                if (checkTypeSlot != null) {
                    ItemStack checkTypeItem = block.getConfigItem(checkTypeSlot);
                    if (checkTypeItem != null && checkTypeItem.hasItemMeta()) {
                        // Extract check type from item
                        return getCheckTypeFromItem(checkTypeItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting check type from container in CheckServerOnlineCondition: " + e.getMessage());
        }
        
        return "online"; // Default check type
    }
    
    /**
     * Extracts check type from an item
     */
    private String getCheckTypeFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the check type
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "online"; // Default check type
    }
}