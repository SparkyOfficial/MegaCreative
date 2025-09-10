package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for setting a block.
 * This action sets a block at the player's location or a specified location from container configuration.
 */
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
            
            if (params.material == null) {
                return ExecutionResult.error("Material is not configured");
            }

            // Resolve any placeholders in the material
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedMaterialName = resolver.resolveString(context, params.materialName);

            // Set the block
            try {
                Material material = Material.valueOf(resolvedMaterialName.toUpperCase());
                Location location = player.getLocation().add(params.relativeX, params.relativeY, params.relativeZ);
                Block blockToSet = location.getBlock();
                blockToSet.setType(material);
                
                return ExecutionResult.success("Set block to " + material.name() + " at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid material: " + params.materialName);
            }
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
                // Get material from the material slot
                Integer materialSlot = slotResolver.apply("material_slot");
                if (materialSlot != null) {
                    ItemStack materialItem = block.getConfigItem(materialSlot);
                    if (materialItem != null && materialItem.hasItemMeta()) {
                        // Extract material from item
                        params.materialName = getMaterialNameFromItem(materialItem);
                        if (params.materialName != null) {
                            try {
                                params.material = Material.valueOf(params.materialName.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                // Use default material if parsing fails
                                params.material = Material.STONE;
                            }
                        }
                    }
                }
                
                // Get relative X from the relative_x slot
                Integer relativeXSlot = slotResolver.apply("relative_x_slot");
                if (relativeXSlot != null) {
                    ItemStack relativeXItem = block.getConfigItem(relativeXSlot);
                    if (relativeXItem != null && relativeXItem.hasItemMeta()) {
                        // Extract relative X from item
                        params.relativeX = getNumberFromItem(relativeXItem, 0);
                    }
                }
                
                // Get relative Y from the relative_y slot
                Integer relativeYSlot = slotResolver.apply("relative_y_slot");
                if (relativeYSlot != null) {
                    ItemStack relativeYItem = block.getConfigItem(relativeYSlot);
                    if (relativeYItem != null && relativeYItem.hasItemMeta()) {
                        // Extract relative Y from item
                        params.relativeY = getNumberFromItem(relativeYItem, 0);
                    }
                }
                
                // Get relative Z from the relative_z slot
                Integer relativeZSlot = slotResolver.apply("relative_z_slot");
                if (relativeZSlot != null) {
                    ItemStack relativeZItem = block.getConfigItem(relativeZSlot);
                    if (relativeZItem != null && relativeZItem.hasItemMeta()) {
                        // Extract relative Z from item
                        params.relativeZ = getNumberFromItem(relativeZItem, 0);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting block parameters from container in SetBlockAction: " + e.getMessage());
        }
        
        // Set defaults if not configured
        if (params.material == null) {
            params.material = Material.STONE;
        }
        
        return params;
    }
    
    /**
     * Extracts material name from an item
     */
    private String getMaterialNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the material name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts number from an item
     */
    private int getNumberFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse number from display name
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
        Material material = null;
        String materialName = "";
        int relativeX = 0;
        int relativeY = 0;
        int relativeZ = 0;
    }
}