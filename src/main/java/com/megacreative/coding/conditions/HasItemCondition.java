package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a player has a specific item from container configuration.
 * This condition returns true if the player has the specified item in their inventory.
 */
public class HasItemCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            HasItemParams params = getItemParamsFromContainer(block, context);
            
            if (params.itemStr == null || params.itemStr.isEmpty()) {
                return false;
            }

            // Resolve any placeholders in the item name
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedItemStr = resolver.resolveString(context, params.itemStr);
            
            // Parse item parameter
            String itemName = resolvedItemStr;
            if (itemName == null || itemName.isEmpty()) {
                return false;
            }

            // Get amount from parameters
            int amount = params.amount;

            // Check if player has the specified item
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                
                // Count how many of this item the player has
                int totalCount = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == material) {
                        totalCount += item.getAmount();
                    }
                }
                
                return totalCount >= amount;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets item parameters from the container configuration
     */
    private HasItemParams getItemParamsFromContainer(CodeBlock block, ExecutionContext context) {
        HasItemParams params = new HasItemParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get item from the item_slot
                Integer itemSlot = slotResolver.apply("item_slot");
                if (itemSlot != null) {
                    ItemStack itemItem = block.getConfigItem(itemSlot);
                    if (itemItem != null) {
                        // Extract item type from item
                        params.itemStr = getItemTypeFromItem(itemItem);
                    }
                }
                
                // Get amount from the amount_slot
                Integer amountSlot = slotResolver.apply("amount_slot");
                if (amountSlot != null) {
                    ItemStack amountItem = block.getConfigItem(amountSlot);
                    if (amountItem != null && amountItem.hasItemMeta()) {
                        // Extract amount from item
                        params.amount = getAmountFromItem(amountItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting item parameters from container in HasItemCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts item type from an item
     */
    private String getItemTypeFromItem(ItemStack item) {
        // For item type, we'll use the item type name
        return item.getType().name();
    }
    
    /**
     * Extracts amount from an item
     */
    private int getAmountFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                try {
                    // Try to parse the amount from the display name
                    String cleanName = displayName.replaceAll("[^0-9]", "");
                    if (!cleanName.isEmpty()) {
                        return Math.max(1, Integer.parseInt(cleanName));
                    }
                } catch (NumberFormatException e) {
                    // Use default amount if parsing fails
                }
            }
        }
        return 1; // Default amount
    }
    
    /**
     * Helper class to hold item parameters
     */
    private static class HasItemParams {
        String itemStr = "";
        int amount = 1;
    }
}