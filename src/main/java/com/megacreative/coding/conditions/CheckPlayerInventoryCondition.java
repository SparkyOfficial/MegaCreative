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
import org.bukkit.inventory.PlayerInventory;

/**
 * Condition for checking if a player has a specific item in their inventory.
 * This condition checks the player's inventory for a specific item and amount.
 */
@BlockMeta(id = "checkPlayerInventory", displayName = "§bCheck Player Inventory", type = BlockType.CONDITION)
public class CheckPlayerInventoryCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the new parameter system
            DataValue itemValue = block.getParameter("item");
            DataValue amountValue = block.getParameter("amount");
            DataValue checkTypeValue = block.getParameter("check_type");
            
            if (itemValue == null || itemValue.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckPlayerInventoryCondition: 'item' parameter is missing.");
                return false;
            }
            
            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedItem = resolver.resolve(context, itemValue);
            
            String itemStr = resolvedItem.asString();
            Material material = Material.matchMaterial(itemStr);
            if (material == null) {
                context.getPlugin().getLogger().warning("CheckPlayerInventoryCondition: Invalid item material '" + itemStr + "'.");
                return false;
            }
            
            // Parse amount (default to 1)
            int amount = 1;
            if (amountValue != null && !amountValue.isEmpty()) {
                DataValue resolvedAmount = resolver.resolve(context, amountValue);
                try {
                    amount = Math.max(1, Integer.parseInt(resolvedAmount.asString()));
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("CheckPlayerInventoryCondition: Invalid amount, using default 1.");
                }
            }
            
            // Parse check type (default to "has")
            String checkType = "has";
            if (checkTypeValue != null && !checkTypeValue.isEmpty()) {
                DataValue resolvedCheckType = resolver.resolve(context, checkTypeValue);
                checkType = resolvedCheckType.asString();
            }
            checkType = checkType != null ? checkType.toLowerCase() : "has";
            
            PlayerInventory inventory = player.getInventory();
            int count = 0;
            
            // Count items of the specified type
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() == material) {
                    count += item.getAmount();
                }
            }
            
            // Evaluate based on check type
            switch (checkType) {
                case "has":
                    return count >= amount;
                case "has_exactly":
                    return count == amount;
                case "has_less_than":
                    return count < amount;
                case "has_more_than":
                    return count > amount;
                default:
                    context.getPlugin().getLogger().warning("CheckPlayerInventoryCondition: Invalid check type '" + checkType + "'.");
                    return false;
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating CheckPlayerInventoryCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold inventory parameters
     */
    private static class CheckPlayerInventoryParams {
        String itemStr = "";
        int amount = 1;
        String checkType = "has";
    }
}