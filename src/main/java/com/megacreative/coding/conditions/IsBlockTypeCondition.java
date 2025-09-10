package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a block at a location is of a specific type from container configuration.
 * This condition returns true if the block at the specified location is of the specified type.
 */
public class IsBlockTypeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            IsBlockTypeParams params = getBlockParamsFromContainer(block, context);
            
            if (params.blockStr == null || params.blockStr.isEmpty()) {
                return false;
            }

            // Parse relative coordinates
            int relativeX = parseCoordinate(params.relativeXStr);
            int relativeY = parseCoordinate(params.relativeYStr);
            int relativeZ = parseCoordinate(params.relativeZStr);

            // Resolve any placeholders in the block type
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedBlockStr = resolver.resolveString(context, params.blockStr);
            
            // Parse block type parameter
            if (resolvedBlockStr == null || resolvedBlockStr.isEmpty()) {
                return false;
            }

            // Check if the block at the specified location is of the specified type
            try {
                Material material = Material.valueOf(resolvedBlockStr.toUpperCase());
                Location location = player.getLocation().add(relativeX, relativeY, relativeZ);
                Block checkBlock = location.getBlock();
                
                return checkBlock.getType() == material;
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
    private IsBlockTypeParams getBlockParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IsBlockTypeParams params = new IsBlockTypeParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get block type from the block slot
                Integer blockSlot = slotResolver.apply("block");
                if (blockSlot != null) {
                    ItemStack blockItem = block.getConfigItem(blockSlot);
                    if (blockItem != null) {
                        // Extract block type from item
                        params.blockStr = getBlockTypeFromItem(blockItem);
                    }
                }
                
                // Get relative X from the relativeX slot
                Integer relativeXSlot = slotResolver.apply("relativeX");
                if (relativeXSlot != null) {
                    ItemStack relativeXItem = block.getConfigItem(relativeXSlot);
                    if (relativeXItem != null && relativeXItem.hasItemMeta()) {
                        // Extract relative X from item
                        params.relativeXStr = getCoordinateFromItem(relativeXItem);
                    }
                }
                
                // Get relative Y from the relativeY slot
                Integer relativeYSlot = slotResolver.apply("relativeY");
                if (relativeYSlot != null) {
                    ItemStack relativeYItem = block.getConfigItem(relativeYSlot);
                    if (relativeYItem != null && relativeYItem.hasItemMeta()) {
                        // Extract relative Y from item
                        params.relativeYStr = getCoordinateFromItem(relativeYItem);
                    }
                }
                
                // Get relative Z from the relativeZ slot
                Integer relativeZSlot = slotResolver.apply("relativeZ");
                if (relativeZSlot != null) {
                    ItemStack relativeZItem = block.getConfigItem(relativeZSlot);
                    if (relativeZItem != null && relativeZItem.hasItemMeta()) {
                        // Extract relative Z from item
                        params.relativeZStr = getCoordinateFromItem(relativeZItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting block parameters from container in IsBlockTypeCondition: " + e.getMessage());
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
     * Extracts coordinate from an item
     */
    private String getCoordinateFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the coordinate
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Parses coordinate string to integer
     */
    private int parseCoordinate(String coordStr) {
        if (coordStr == null || coordStr.isEmpty()) {
            return 0;
        }
        
        try {
            return Integer.parseInt(coordStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Helper class to hold block parameters
     */
    private static class IsBlockTypeParams {
        String blockStr = "";
        String relativeXStr = "";
        String relativeYStr = "";
        String relativeZStr = "";
    }
}