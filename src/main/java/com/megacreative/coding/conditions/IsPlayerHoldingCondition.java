package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a player is holding a specific item from container configuration.
 * This condition returns true if the player is holding the specified item in their main hand.
 */
public class IsPlayerHoldingCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            IsPlayerHoldingParams params = getItemParamsFromContainer(block, context);
            
            if (params.itemStr == null || params.itemStr.isEmpty()) {
                return false;
            }

            // Resolve any placeholders in the item name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue itemValue = DataValue.of(params.itemStr);
            DataValue resolvedItem = resolver.resolve(context, itemValue);
            
            // Parse item parameter
            String itemName = resolvedItem.asString();
            if (itemName == null || itemName.isEmpty()) {
                return false;
            }

            // Check if player is holding the specified item
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                
                return itemInHand != null && itemInHand.getType() == material;
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
    private IsPlayerHoldingParams getItemParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IsPlayerHoldingParams params = new IsPlayerHoldingParams();
        
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
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting item parameters from container in IsPlayerHoldingCondition: " + e.getMessage());
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
     * Helper class to hold item parameters
     */
    private static class IsPlayerHoldingParams {
        String itemStr = "";
    }
}