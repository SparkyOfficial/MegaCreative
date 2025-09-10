package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Condition for checking if a player is wearing specific armor.
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
            // Get the armor parameter from the block
            DataValue armorValue = block.getParameter("armor");
            if (armorValue == null) {
                return false;
            }

            // Resolve any placeholders in the armor name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedArmor = resolver.resolve(context, armorValue);
            
            // Parse armor parameter
            String armorName = resolvedArmor.asString();
            if (armorName == null || armorName.isEmpty()) {
                return false;
            }

            // Check if player is wearing the specified armor
            try {
                Material material = Material.valueOf(armorName.toUpperCase());
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
}