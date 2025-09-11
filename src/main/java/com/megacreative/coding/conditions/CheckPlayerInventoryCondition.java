package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

public class CheckPlayerInventoryCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;

        try {
            // Get parameters from the container configuration
            CheckPlayerInventoryParams params = getInventoryParamsFromContainer(block, context);
            
            if (params.itemStr == null || params.itemStr.isEmpty()) {
                context.getPlugin().getLogger().warning("InventoryCheck: 'item' parameter is missing.");
                return false;
            }

            Material material = Material.matchMaterial(params.itemStr);
            if (material == null) {
                context.getPlugin().getLogger().warning("InventoryCheck: Invalid material '" + params.itemStr + "'.");
                return false;
            }
            
            int amount = params.amount;
            String checkType = params.checkType.toLowerCase();

            switch (checkType) {
                case "has":
                    return player.getInventory().containsAtLeast(new ItemStack(material), amount);
                case "missing":
                    return !player.getInventory().containsAtLeast(new ItemStack(material), amount);
                case "exact":
                    int count = 0;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == material) {
                            count += item.getAmount();
                        }
                    }
                    return count == amount;
                default:
                    return false;
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating CheckPlayerInventoryCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets inventory parameters from the container configuration
     */
    private CheckPlayerInventoryParams getInventoryParamsFromContainer(CodeBlock block, ExecutionContext context) {
        CheckPlayerInventoryParams params = new CheckPlayerInventoryParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get item from the item_slot
                Integer itemSlot = slotResolver.apply("item_slot");
                if (itemSlot != null) {
                    ItemStack itemItem = block.getConfigItem(itemSlot);
                    if (itemItem != null) {
                        // Extract item type from item
                        params.itemStr = getItemTypeFromItem(itemItem);
                    }
                }
                
                // Get amount from the amount_slot
                Integer amountSlot = slotResolver.apply("amount_slot");
                if (amountSlot != null) {
                    ItemStack amountItem = block.getConfigItem(amountSlot);
                    if (amountItem != null && amountItem.hasItemMeta()) {
                        // Extract amount from item
                        params.amount = getAmountFromItem(amountItem);
                    }
                }
                
                // Get check type from the check_type_slot
                Integer checkTypeSlot = slotResolver.apply("check_type_slot");
                if (checkTypeSlot != null) {
                    ItemStack checkTypeItem = block.getConfigItem(checkTypeSlot);
                    if (checkTypeItem != null && checkTypeItem.hasItemMeta()) {
                        // Extract check type from item
                        params.checkType = getCheckTypeFromItem(checkTypeItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting inventory parameters from container in CheckPlayerInventoryCondition: " + e.getMessage());
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