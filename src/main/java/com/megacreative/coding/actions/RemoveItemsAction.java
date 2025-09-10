package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ListValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * Action for removing items from a player's inventory.
 * This action removes a list of items from the player's inventory based on container configuration.
 */
public class RemoveItemsAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get items from the container configuration
            List<ItemStack> itemsToRemove = getItemsFromContainer(block, context);
            
            if (itemsToRemove.isEmpty()) {
                return ExecutionResult.error("No items configured for removal");
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
     * Gets items from the container configuration
     */
    private List<ItemStack> getItemsFromContainer(CodeBlock block, ExecutionContext context) {
        List<ItemStack> items = new ArrayList<>();
        
        try {
            // Get the BlockConfigService to resolve item groups
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the group slots resolver for this action
            Function<String, int[]> groupSlotsResolver = blockConfigService.getGroupSlotsResolver(block.getAction());
            
            if (groupSlotsResolver != null) {
                // Get items from the "items_to_remove" group
                List<ItemStack> groupItems = block.getItemsFromNamedGroup("items_to_remove", 
                    groupName -> {
                        int[] slots = groupSlotsResolver.apply(groupName);
                        if (slots != null) {
                            List<Integer> slotList = new ArrayList<>();
                            for (int slot : slots) {
                                slotList.add(slot);
                            }
                            return slotList;
                        }
                        return new ArrayList<>();
                    });
                
                items.addAll(groupItems);
            }
            
            // If no items from groups, try to get items from individual slots
            if (items.isEmpty()) {
                // Try to get items from config items directly
                for (int i = 0; i < 9; i++) { // Standard chest size
                    ItemStack item = block.getConfigItem(i);
                    if (item != null && !item.getType().isAir()) {
                        items.add(item);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting items from container in RemoveItemsAction: " + e.getMessage());
        }
        
        return items;
    }
}