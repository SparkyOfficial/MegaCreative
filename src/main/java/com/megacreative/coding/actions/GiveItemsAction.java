package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * Action for giving items to a player.
 * Supports both legacy configuration and new named slot system.
 */
public class GiveItemsAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player available to give items to");
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
                configService != null ? configService.getGroupSlotsResolver("giveItems") : null;

            // 3. Get all items from the named group "items_to_give"
            List<ItemStack> itemsToGive = new ArrayList<>();
            if (groupSlotsResolver != null) {
                int[] slots = groupSlotsResolver.apply("items_to_give");
                if (slots != null) {
                    for (int slot : slots) {
                        ItemStack item = actionBlock.getConfigItem(slot);
                        if (item != null) {
                            itemsToGive.add(item);
                        }
                    }
                }
            }
            
            // If group not found, use fallback method for compatibility
            if (itemsToGive.isEmpty()) {
                // Fallback to slots 0-8
                for (int i = 0; i < 9; i++) {
                    ItemStack item = actionBlock.getConfigItem(i);
                    if (item != null) {
                        itemsToGive.add(item);
                    }
                }
            }

            // 4. Give items to player
            int givenItems = 0;
            for (ItemStack item : itemsToGive) {
                if (item != null) {
                    // Create a copy of the item to give
                    ItemStack itemToGive = item.clone();
                    
                    // Get amount from parameter or use 1
                    int amount = 1;
                    Object amountParam = actionBlock.getParameter("amount");
                    if (amountParam != null) {
                        try {
                            amount = Integer.parseInt(amountParam.toString());
                        } catch (NumberFormatException e) {
                            // Use default value
                        }
                    }
                    
                    itemToGive.setAmount(amount);
                    
                    // Give the item
                    player.getInventory().addItem(itemToGive);
                    givenItems++;
                }
            }

            // 5. Notify player
            if (givenItems > 0) {
                return ExecutionResult.success("Given " + givenItems + " items to player");
            } else {
                return ExecutionResult.success("No items to give");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error giving items: " + e.getMessage());
        }
    }
}