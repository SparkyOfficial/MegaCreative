package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Condition for checking if a player has a specific item.
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
            // Get the item parameter from the block
            DataValue itemValue = block.getParameter("item");
            if (itemValue == null) {
                return false;
            }

            // Resolve any placeholders in the item name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedItem = resolver.resolve(context, itemValue);
            
            // Parse item parameter
            String itemName = resolvedItem.asString();
            if (itemName == null || itemName.isEmpty()) {
                return false;
            }

            // Get optional amount parameter (default to 1)
            int amount = 1;
            DataValue amountValue = block.getParameter("amount");
            if (amountValue != null) {
                DataValue resolvedAmount = resolver.resolve(context, amountValue);
                try {
                    amount = Math.max(1, resolvedAmount.asNumber().intValue());
                } catch (NumberFormatException e) {
                    // Use default amount if parsing fails
                }
            }

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
}