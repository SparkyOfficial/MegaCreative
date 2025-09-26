package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for setting a block at a location.
 * This action retrieves block parameters from the container configuration and sets the block.
 */
@BlockMeta(id = "setBlock", displayName = "§aSet Block", type = BlockType.ACTION)
public class SetBlockAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get block parameters from the container configuration
            SetBlockParams params = getBlockParamsFromContainer(block, context);
            
            if (params.blockNameStr == null || params.blockNameStr.isEmpty()) {
                return ExecutionResult.error("Block is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue blockNameVal = DataValue.of(params.blockNameStr);
            DataValue resolvedBlockName = resolver.resolve(context, blockNameVal);
            
            // Parse parameters
            String blockName = resolvedBlockName.asString();
            int relativeX = params.relativeX;
            int relativeY = params.relativeY;
            int relativeZ = params.relativeZ;

            // Parse the block material
            Material material;
            try {
                material = Material.valueOf(blockName.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default material if parsing fails
                material = Material.STONE;
            }

            // Calculate the target location
            Location playerLocation = player.getLocation();
            Location targetLocation = playerLocation.clone().add(relativeX, relativeY, relativeZ);
            
            // Set the actual block in the world
            targetLocation.getBlock().setType(material);
            
            context.getPlugin().getLogger().info("Setting block " + material + " at relative position (" + relativeX + ", " + relativeY + ", " + relativeZ + ")");
            
            return ExecutionResult.success("Block set successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set block: " + e.getMessage());
        }
    }
    
    /**
     * Gets block parameters from the container configuration
     */
    private SetBlockParams getBlockParamsFromContainer(CodeBlock block, ExecutionContext context) {
        SetBlockParams params = new SetBlockParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get block name from the block slot
                Integer blockSlot = slotResolver.apply("block_slot");
                if (blockSlot != null) {
                    ItemStack blockItem = block.getConfigItem(blockSlot);
                    if (blockItem != null) {
                        // Extract block name from item
                        params.blockNameStr = getBlockNameFromItem(blockItem);
                    }
                }
                
                // Get relative X from the relativeX slot
                Integer relativeXSlot = slotResolver.apply("relativeX_slot");
                if (relativeXSlot != null) {
                    ItemStack relativeXItem = block.getConfigItem(relativeXSlot);
                    if (relativeXItem != null && relativeXItem.hasItemMeta()) {
                        // Extract relative X from item
                        params.relativeX = getRelativeXFromItem(relativeXItem, 0);
                    }
                }
                
                // Get relative Y from the relativeY slot
                Integer relativeYSlot = slotResolver.apply("relativeY_slot");
                if (relativeYSlot != null) {
                    ItemStack relativeYItem = block.getConfigItem(relativeYSlot);
                    if (relativeYItem != null && relativeYItem.hasItemMeta()) {
                        // Extract relative Y from item
                        params.relativeY = getRelativeYFromItem(relativeYItem, 0);
                    }
                }
                
                // Get relative Z from the relativeZ slot
                Integer relativeZSlot = slotResolver.apply("relativeZ_slot");
                if (relativeZSlot != null) {
                    ItemStack relativeZItem = block.getConfigItem(relativeZSlot);
                    if (relativeZItem != null && relativeZItem.hasItemMeta()) {
                        // Extract relative Z from item
                        params.relativeZ = getRelativeZFromItem(relativeZItem, 0);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting block parameters from container in SetBlockAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts block name from an item
     */
    private String getBlockNameFromItem(ItemStack item) {
        // For block name, we'll use the item type name
        return item.getType().name();
    }
    
    /**
     * Extracts relative X from an item
     */
    private int getRelativeXFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse relative X from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Integer.parseInt(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Extracts relative Y from an item
     */
    private int getRelativeYFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse relative Y from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Integer.parseInt(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Extracts relative Z from an item
     */
    private int getRelativeZFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse relative Z from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Integer.parseInt(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Helper class to hold block parameters
     */
    private static class SetBlockParams {
        String blockNameStr = "";
        int relativeX = 0;
        int relativeY = 0;
        int relativeZ = 0;
    }
}