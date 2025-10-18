package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Condition for checking if a player is holding a specific item from the new parameter system.
 * This condition returns true if the player is holding the specified item in their main hand.
 */
@BlockMeta(id = "isPlayerHolding", displayName = "§aIs Player Holding", type = BlockType.CONDITION)
public class IsPlayerHoldingCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            
            DataValue itemValue = block.getParameter("item");
            
            if (itemValue == null || itemValue.isEmpty()) {
                context.getPlugin().getLogger().warning("IsPlayerHoldingCondition: 'item' parameter is missing.");
                return false;
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedItem = resolver.resolve(context, itemValue);
            
            
            String itemName = resolvedItem.asString();
            if (itemName == null || itemName.isEmpty()) {
                context.getPlugin().getLogger().warning("IsPlayerHoldingCondition: 'item' parameter is empty.");
                return false;
            }

            
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                
                return itemInHand.getType() == material;
            } catch (IllegalArgumentException e) {
                context.getPlugin().getLogger().warning("IsPlayerHoldingCondition: Invalid item material '" + itemName + "'.");
                return false;
            }
        } catch (Exception e) {
            
            context.getPlugin().getLogger().warning("Error in IsPlayerHoldingCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold item parameters
     */
    private static class IsPlayerHoldingParams {
        String itemStr = "";
    }
}