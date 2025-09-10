package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ListValue;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Action for removing items from a player's inventory.
 * This action removes a list of items from the player's inventory.
 */
public class RemoveItemsAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the items parameter from the block
            DataValue itemsValue = block.getParameter("items");
            if (itemsValue == null) {
                return ExecutionResult.error("Items parameter is missing");
            }

            // Resolve any placeholders in the items
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedItems = resolver.resolve(context, itemsValue);
            
            // Check if the resolved value is a list
            if (resolvedItems instanceof ListValue) {
                ListValue listValue = (ListValue) resolvedItems;
                List<DataValue> itemsList = listValue.getList(); // Changed from getValues() to getList()
                int removedCount = 0;
                
                // Remove each item from the player's inventory
                for (DataValue itemValue : itemsList) {
                    if (itemValue.getValue() instanceof ItemStack) {
                        ItemStack item = (ItemStack) itemValue.getValue();
                        if (player.getInventory().containsAtLeast(item, item.getAmount())) {
                            player.getInventory().removeItem(item);
                            removedCount += item.getAmount();
                        }
                    }
                }
                
                return ExecutionResult.success("Removed " + removedCount + " items from player's inventory");
            } else {
                return ExecutionResult.error("Items parameter is not a list");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to remove items: " + e.getMessage());
        }
    }
}