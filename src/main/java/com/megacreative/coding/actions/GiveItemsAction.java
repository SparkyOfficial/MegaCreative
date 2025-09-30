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
 * Action for giving multiple items to a player.
 * This action gives a list of items to the player based on parameters.
 */
@BlockMeta(id = "giveItems", displayName = "§aGive Items", type = BlockType.ACTION)
public class GiveItemsAction implements BlockAction {

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
                return ExecutionResult.error("No items configured for giving");
            }

            List<ItemStack> itemsToGive = new ArrayList<>();
            
            // Handle different types of item specifications
            if (itemsValue instanceof ListValue) {
                // Handle list of items
                ListValue listValue = (ListValue) itemsValue;
                List<DataValue> itemList = listValue.getValues();
                for (DataValue itemValue : itemList) {
                    ItemStack item = parseItem(itemValue);
                    if (item != null) {
                        itemsToGive.add(item);
                    }
                }
            } else {
                // Handle single item
                ItemStack item = parseItem(itemsValue);
                if (item != null) {
                    itemsToGive.add(item);
                }
            }

            if (itemsToGive.isEmpty()) {
                return ExecutionResult.error("No valid items to give");
            }

            int itemCount = 0;
            
            // Give each item to the player
            for (ItemStack item : itemsToGive) {
                if (item != null && item.getType().isItem()) {
                    player.getInventory().addItem(item.clone());
                    itemCount++;
                }
            }
            
            return ExecutionResult.success("Gave " + itemCount + " items to player");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to give items: " + e.getMessage());
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