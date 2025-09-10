package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a player is wearing specific armor from container configuration.
 * This condition returns true if the player is wearing the specified armor piece.
 */
public class HasArmorCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            HasArmorParams params = getArmorParamsFromContainer(block, context);
            
            if (params.armorStr == null || params.armorStr.isEmpty()) {
                return false;
            }

            // Resolve any placeholders in the armor name
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedArmorStr = resolver.resolveString(context, params.armorStr);
            
            // Parse armor parameter
            if (resolvedArmorStr == null || resolvedArmorStr.isEmpty()) {
                return false;
            }

            // Check if player is wearing the specified armor
            try {
                Material material = Material.valueOf(resolvedArmorStr.toUpperCase());
                ItemStack helmet = player.getInventory().getHelmet();
                ItemStack chestplate = player.getInventory().getChestplate();
                ItemStack leggings = player.getInventory().getLeggings();
                ItemStack boots = player.getInventory().getBoots();
                
                // Check each armor slot
                return (helmet != null && helmet.getType() == material) ||
                       (chestplate != null && chestplate.getType() == material) ||
                       (leggings != null && leggings.getType() == material) ||
                       (boots != null && boots.getType() == material);
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets armor parameters from the container configuration
     */
    private HasArmorParams getArmorParamsFromContainer(CodeBlock block, ExecutionContext context) {
        HasArmorParams params = new HasArmorParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get armor from the armor_slot
                Integer armorSlot = slotResolver.apply("armor_slot");
                if (armorSlot != null) {
                    ItemStack armorItem = block.getConfigItem(armorSlot);
                    if (armorItem != null) {
                        // Extract armor type from item
                        params.armorStr = getArmorTypeFromItem(armorItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting armor parameters from container in HasArmorCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts armor type from an item
     */
    private String getArmorTypeFromItem(ItemStack item) {
        // For armor type, we'll use the item type name
        return item.getType().name();
    }
    
    /**
     * Helper class to hold armor parameters
     */
    private static class HasArmorParams {
        String armorStr = "";
    }
}