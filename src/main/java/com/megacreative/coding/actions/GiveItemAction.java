package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta; // Added import
import com.megacreative.coding.BlockType; // Added import
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

/**
 * Action for giving an item to a player.
 * This action retrieves an item from the container configuration and gives it to the player.
 */
@BlockMeta(id = "giveItem", displayName = "§aGive Item", type = BlockType.ACTION) // Added annotation
public class GiveItemAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            ItemStack item = getItemFromContainer(block, context);
            
            if (item == null) {
                return ExecutionResult.error("No item configured");
            }

            player.getInventory().addItem(item.clone());
            return ExecutionResult.success("Item given to player successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to give item: " + e.getMessage());
        }
    }
    
    /**
     * 🎆 ENHANCED: Gets item from the container configuration with improved handling
     */
    private ItemStack getItemFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get item from the item slot (primary)
                Integer itemSlot = slotResolver.apply("item");
                if (itemSlot == null) {
                    itemSlot = slotResolver.apply("item_slot"); // Fallback
                }
                
                if (itemSlot != null) {
                    ItemStack configuredItem = block.getConfigItem(itemSlot);
                    if (configuredItem != null && !configuredItem.getType().isAir()) {
                        // Get amount from amount slot if configured
                        Integer amountSlot = slotResolver.apply("amount");
                        if (amountSlot != null) {
                            ItemStack amountItem = block.getConfigItem(amountSlot);
                            if (amountItem != null && amountItem.hasItemMeta()) {
                                String amountStr = org.bukkit.ChatColor.stripColor(
                                    amountItem.getItemMeta().getDisplayName()).trim();
                                try {
                                    int amount = Integer.parseInt(amountStr);
                                    configuredItem.setAmount(Math.max(1, Math.min(64, amount)));
                                } catch (NumberFormatException e) {
                                    // Use default amount from configured item
                                }
                            }
                        }
                        return configuredItem;
                    }
                }
            }
            
            // 🎆 ENHANCED: Fallback to parameter-based configuration
            com.megacreative.coding.values.DataValue materialParam = block.getParameter("material");
            com.megacreative.coding.values.DataValue amountParam = block.getParameter("amount");
            
            if (materialParam != null && !materialParam.isEmpty()) {
                try {
                    org.bukkit.Material material = org.bukkit.Material.valueOf(materialParam.asString().toUpperCase());
                    int amount = 1;
                    if (amountParam != null && !amountParam.isEmpty()) {
                        amount = Math.max(1, Math.min(64, Integer.parseInt(amountParam.asString())));
                    }
                    return new ItemStack(material, amount);
                } catch (Exception e) {
                    // Handle exception
                }
            }
            
        } catch (Exception e) {
            // Suppress exception
        }
        
        return null;
    }
}