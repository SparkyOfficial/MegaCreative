package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.List;
import java.util.ArrayList;

/**
 * Action for removing items from a player's inventory.
 * This action removes a list of items from the player's inventory based on parameters.
 */
@BlockMeta(id = "removeItems", displayName = "§aRemove Items", type = BlockType.ACTION)
public class RemoveItemsAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get items parameter from the new parameter system
            DataValue itemsValue = block.getParameter("items");
            
            if (itemsValue == null || itemsValue.isEmpty()) {
                return ExecutionResult.error("No items configured for removal");
            }

            List<ItemStack> itemsToRemove = new ArrayList<>();
            
            // Handle different types of item specifications
            if (itemsValue instanceof ListValue) {
                // Handle list of items
                ListValue listValue = (ListValue) itemsValue;
                List<DataValue> itemList = listValue.getValues();
                for (DataValue itemValue : itemList) {
                    ItemStack item = parseItem(itemValue);
                    if (item != null) {
                        itemsToRemove.add(item);
                    }
                }
            } else {
                // Handle single item
                ItemStack item = parseItem(itemsValue);
                if (item != null) {
                    itemsToRemove.add(item);
                }
            }

            if (itemsToRemove.isEmpty()) {
                return ExecutionResult.error("No valid items to remove");
            }

            int removedCount = 0;
            
            // Remove each item from the player's inventory
            for (ItemStack item : itemsToRemove) {
                if (item != null && item.getType().isItem()) {
                    if (player.getInventory().containsAtLeast(item, item.getAmount())) {
                        player.getInventory().removeItem(item);
                        removedCount += item.getAmount();
                    }
                }
            }
            
            return ExecutionResult.success("Removed " + removedCount + " items from player's inventory");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to remove items: " + e.getMessage());
        }
    }
    
    /**
     * Parses an item from a DataValue
     */
    private ItemStack parseItem(DataValue itemValue) {
        try {
            if (itemValue == null || itemValue.isEmpty()) {
                return null;
            }
            
            String itemStr = itemValue.asString();
            if (itemStr == null || itemStr.isEmpty()) {
                return null;
            }
            
            // Parse format: MATERIAL:AMOUNT or just MATERIAL
            String[] parts = itemStr.split(":");
            Material material = Material.valueOf(parts[0].toUpperCase());
            
            int amount = 1;
            if (parts.length > 1) {
                try {
                    amount = Integer.parseInt(parts[1]);
                    amount = Math.max(1, Math.min(64, amount)); // Clamp between 1 and 64
                } catch (NumberFormatException e) {
                    // Use default amount
                }
            }
            
            return new ItemStack(material, amount);
        } catch (Exception e) {
            return null;
        }
    }
}