package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

/**
 * Action for giving an item to a player.
 * This action retrieves an item from the container configuration and gives it to the player.
 */
public class GiveItemAction implements BlockAction {

    @Override
public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the item from the container configuration
            ItemStack item = getItemFromContainer(block, context);
            
            if (item == null) {
                return ExecutionResult.error("Item is not configured");
            }

            // Give the item to the player
            player.getInventory().addItem(item.clone());
            return ExecutionResult.success("Item given to player successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to give item to player: " + e.getMessage());
        }
    }
    
    /**
     * Gets item from the container configuration
     */
    private ItemStack getItemFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get item from the item slot
                Integer itemSlot = slotResolver.apply("item_slot");
                if (itemSlot != null) {
                    return block.getConfigItem(itemSlot);
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting item from container in GiveItemAction: " + e.getMessage());
        }
        
        return null;
    }
}