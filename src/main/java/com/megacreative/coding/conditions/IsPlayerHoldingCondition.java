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
 * Condition for checking if a player is holding a specific item.
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
}