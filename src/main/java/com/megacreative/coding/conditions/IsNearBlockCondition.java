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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a specific block type is near the player from container configuration.
 * This condition returns true if the specified block type is within a specified distance of the player.
 */
@BlockMeta(id = "isNearBlock", displayName = "§aIs Near Block", type = BlockType.CONDITION)
public class IsNearBlockCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            IsNearBlockParams params = getBlockParamsFromContainer(block, context);
            
            // Parse block type parameter
            String blockName = params.blockStr;
            if (blockName == null || blockName.isEmpty()) {
                return false;
            }

            // Parse distance parameter (default to 5)
            int distance = 5;
            if (params.distanceStr != null && !params.distanceStr.isEmpty()) {
                try {
                    distance = Math.max(1, Integer.parseInt(params.distanceStr));
                } catch (NumberFormatException e) {
                    // Use default distance if parsing fails
                }
            }

            // Check if the specified block type is near the player
            try {
                Material material = Material.valueOf(blockName.toUpperCase());
                
                // Check blocks around the player within the specified distance
                for (int x = -distance; x <= distance; x++) {
                    for (int y = -distance; y <= distance; y++) {
                        for (int z = -distance; z <= distance; z++) {
                            if (player.getLocation().getBlock().getRelative(x, y, z).getType() == material) {
                                return true;
                            }
                        }
                    }
                }
                
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets block parameters from the container configuration
     */
    private IsNearBlockParams getBlockParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IsNearBlockParams params = new IsNearBlockParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get block from the block_slot
                Integer blockSlot = slotResolver.apply("block_slot");
                if (blockSlot != null) {
                    ItemStack blockItem = block.getConfigItem(blockSlot);
                    if (blockItem != null) {
                        // Extract block type from item
                        params.blockStr = getBlockTypeFromItem(blockItem);
                    }
                }
                
                // Get distance from the distance_slot
                Integer distanceSlot = slotResolver.apply("distance_slot");
                if (distanceSlot != null) {
                    ItemStack distanceItem = block.getConfigItem(distanceSlot);
                    if (distanceItem != null && distanceItem.hasItemMeta()) {
                        // Extract distance from item
                        params.distanceStr = getDistanceFromItem(distanceItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting block parameters from container in IsNearBlockCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts block type from an item
     */
    private String getBlockTypeFromItem(ItemStack item) {
        // For block type, we'll use the item type name
        return item.getType().name();
    }
    
    /**
     * Extracts distance from an item
     */
    private String getDistanceFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the distance
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Helper class to hold block parameters
     */
    private static class IsNearBlockParams {
        String blockStr = "";
        String distanceStr = "";
    }
}