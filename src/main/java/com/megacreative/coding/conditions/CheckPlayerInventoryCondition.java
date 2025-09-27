package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

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
            CheckPlayerInventoryParams params = getInventoryParams(block, context);
            if (params == null) {
                return false;
            }

            Material material = Material.matchMaterial(params.itemStr);
            if (material == null) {
                return false;
            }
            
            int amount = params.amount;
            String checkType = params.checkType.toLowerCase();
            
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
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets inventory parameters from the block configuration
     */
    private CheckPlayerInventoryParams getInventoryParams(CodeBlock block, ExecutionContext context) {
        CheckPlayerInventoryParams params = new CheckPlayerInventoryParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get item type from item slot
                Integer itemSlot = slotResolver.apply("item");
                if (itemSlot != null) {
                    ItemStack item = block.getConfigItem(itemSlot);
                    if (item != null) {
                        // Extract item type from item
                        params.itemStr = getItemTypeFromItem(item);
                        
                        // Extract amount from amount slot or item
                        Integer amountSlot = slotResolver.apply("amount");
                        if (amountSlot != null) {
                            ItemStack amountItem = block.getConfigItem(amountSlot);
                            if (amountItem != null) {
                                params.amount = getAmountFromItem(amountItem);
                            }
                        } else {
                            // Extract amount from item
                            params.amount = getAmountFromItem(item);
                        }
                        
                        // Extract check type from check type slot or item
                        Integer checkTypeSlot = slotResolver.apply("check_type");
                        if (checkTypeSlot != null) {
                            ItemStack checkTypeItem = block.getConfigItem(checkTypeSlot);
                            if (checkTypeItem != null) {
                                params.checkType = getCheckTypeFromItem(checkTypeItem);
                            }
                        } else {
                            // Extract check type from item
                            params.checkType = getCheckTypeFromItem(item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Suppress exception
        }
        
        return params;
    }
    
    /**
     * Extracts item type from an item
     */
    private String getItemTypeFromItem(ItemStack item) {
        // For item type, we'll use the item type name
        return item.getType().name();
    }
    
    /**
     * Extracts amount from an item
     */
    private int getAmountFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                try {
                    // Try to parse the amount from the display name
                    String cleanName = displayName.replaceAll("[^0-9]", "");
                    if (!cleanName.isEmpty()) {
                        return Math.max(1, Integer.parseInt(cleanName));
                    }
                } catch (NumberFormatException e) {
                    // Use default amount if parsing fails
                }
            }
        }
        return 1; // Default amount
    }
    
    /**
     * Extracts check type from an item
     */
    private String getCheckTypeFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Return the check type from the display name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "has"; // Default check type
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