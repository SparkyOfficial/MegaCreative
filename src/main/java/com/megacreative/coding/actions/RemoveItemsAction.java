package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * Action for removing items from a player's inventory.
 * Supports both legacy configuration and new named slot system.
 */
public class RemoveItemsAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player available to remove items from");
        }

        try {
            // 1. Get our code block and its configuration
            CodeBlock actionBlock = context.getCurrentBlock();
            if (actionBlock == null) {
                return ExecutionResult.error("No action block found");
            }

            // 2. Get group slots resolver from BlockConfigService
            BlockConfigService configService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            Function<String, int[]> groupSlotsResolver = 
                configService != null ? configService.getGroupSlotsResolver("removeItems") : null;

            // 3. Get all items from the named group "items_to_remove"
            List<ItemStack> itemsToRemove = new ArrayList<>();
            if (groupSlotsResolver != null) {
                int[] slots = groupSlotsResolver.apply("items_to_remove");
                if (slots != null) {
                    for (int slot : slots) {
                        ItemStack item = actionBlock.getConfigItem(slot);
                        if (item != null) {
                            itemsToRemove.add(item);
                        }
                    }
                }
            }
            
            // If group not found, use fallback method for compatibility
            if (itemsToRemove.isEmpty()) {
                // Fallback to slots 0-8
                for (int i = 0; i < 9; i++) {
                    ItemStack item = actionBlock.getConfigItem(i);
                    if (item != null) {
                        itemsToRemove.add(item);
                    }
                }
            }

            // 4. Remove items from player
            int removedItems = 0;
            for (ItemStack item : itemsToRemove) {
                if (item != null) {
                    Material material = item.getType();
                    int amount = item.getAmount();
                    
                    // Remove items of the specified type and amount
                    ItemStack[] contents = player.getInventory().getContents();
                    for (int i = 0; i < contents.length && amount > 0; i++) {
                        ItemStack inventoryItem = contents[i];
                        if (inventoryItem != null && inventoryItem.getType() == material) {
                            int toRemove = Math.min(amount, inventoryItem.getAmount());
                            if (inventoryItem.getAmount() <= toRemove) {
                                player.getInventory().setItem(i, null);
                            } else {
                                inventoryItem.setAmount(inventoryItem.getAmount() - toRemove);
                            }
                            amount -= toRemove;
                            removedItems += toRemove;
                        }
                    }
                }
            }

            // 5. Notify player
            if (removedItems > 0) {
                return ExecutionResult.success("Removed " + removedItems + " items from player");
            } else {
                return ExecutionResult.success("No items to remove");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error removing items: " + e.getMessage());
        }
    }
}