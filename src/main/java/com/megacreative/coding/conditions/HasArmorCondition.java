package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Condition for checking if a player is wearing specific armor from the new parameter system.
 * This condition returns true if the player is wearing the specified armor piece.
 */
@BlockMeta(id = "hasArmor", displayName = "§aHas Armor", type = BlockType.CONDITION)
public class HasArmorCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the new parameter system
            DataValue armorValue = block.getParameter("armor");
            
            if (armorValue == null || armorValue.isEmpty()) {
                context.getPlugin().getLogger().warning("HasArmorCondition: 'armor' parameter is missing.");
                return false;
            }

            // Resolve any placeholders in the armor name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedArmor = resolver.resolve(context, armorValue);
            
            // Parse armor parameter
            String armorName = resolvedArmor.asString();
            if (armorName == null || armorName.isEmpty()) {
                context.getPlugin().getLogger().warning("HasArmorCondition: 'armor' parameter is empty.");
                return false;
            }

            // Check if player is wearing the specified armor
            try {
                Material material = Material.valueOf(armorName.toUpperCase());
                PlayerInventory inventory = player.getInventory();
                ItemStack helmet = inventory.getHelmet();
                ItemStack chestplate = inventory.getChestplate();
                ItemStack leggings = inventory.getLeggings();
                ItemStack boots = inventory.getBoots();
                
                // Check each armor slot
                return (helmet != null && helmet.getType() == material) ||
                       (chestplate != null && chestplate.getType() == material) ||
                       (leggings != null && leggings.getType() == material) ||
                       (boots != null && boots.getType() == material);
            } catch (IllegalArgumentException e) {
                context.getPlugin().getLogger().warning("HasArmorCondition: Invalid armor material '" + armorName + "'.");
                return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            context.getPlugin().getLogger().warning("Error in HasArmorCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold armor parameters
     */
    private static class HasArmorParams {
        String armorStr = "";
    }
}