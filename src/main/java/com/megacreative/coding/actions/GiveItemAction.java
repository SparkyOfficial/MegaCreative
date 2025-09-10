package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Action for giving an item to a player.
 * This action retrieves an item from the block parameters and gives it to the player.
 */
public class GiveItemAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the item parameter from the block
            DataValue itemValue = block.getParameter("item");
            if (itemValue == null) {
                return ExecutionResult.error("Item parameter is missing");
            }

            // Resolve any placeholders in the item
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedItem = resolver.resolve(context, itemValue);
            
            // Check if the resolved value is an ItemValue
            if (resolvedItem instanceof com.megacreative.coding.values.ItemValue) {
                ItemStack item = (ItemStack) resolvedItem.getValue();
                if (item != null) {
                    player.getInventory().addItem(item);
                    return ExecutionResult.success("Item given to player successfully");
                } else {
                    return ExecutionResult.error("Item is null");
                }
            } else {
                return ExecutionResult.error("Item parameter is not a valid ItemValue");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to give item to player: " + e.getMessage());
        }
    }
}