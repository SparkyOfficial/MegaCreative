package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Action for setting a player's armor.
 * This action sets the player's armor pieces.
 */
public class SetArmorAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the helmet parameter from the block (optional)
            DataValue helmetValue = block.getParameter("helmet");
            
            // Get the chestplate parameter from the block (optional)
            DataValue chestplateValue = block.getParameter("chestplate");
            
            // Get the leggings parameter from the block (optional)
            DataValue leggingsValue = block.getParameter("leggings");
            
            // Get the boots parameter from the block (optional)
            DataValue bootsValue = block.getParameter("boots");

            // Resolve any placeholders in the armor items
            ParameterResolver resolver = new ParameterResolver(context);
            
            // Set the armor pieces
            if (helmetValue != null) {
                DataValue resolvedHelmet = resolver.resolve(context, helmetValue);
                if (resolvedHelmet.getValue() instanceof ItemStack) {
                    player.getInventory().setHelmet((ItemStack) resolvedHelmet.getValue());
                }
            }
            
            if (chestplateValue != null) {
                DataValue resolvedChestplate = resolver.resolve(context, chestplateValue);
                if (resolvedChestplate.getValue() instanceof ItemStack) {
                    player.getInventory().setChestplate((ItemStack) resolvedChestplate.getValue());
                }
            }
            
            if (leggingsValue != null) {
                DataValue resolvedLeggings = resolver.resolve(context, leggingsValue);
                if (resolvedLeggings.getValue() instanceof ItemStack) {
                    player.getInventory().setLeggings((ItemStack) resolvedLeggings.getValue());
                }
            }
            
            if (bootsValue != null) {
                DataValue resolvedBoots = resolver.resolve(context, bootsValue);
                if (resolvedBoots.getValue() instanceof ItemStack) {
                    player.getInventory().setBoots((ItemStack) resolvedBoots.getValue());
                }
            }
            
            return ExecutionResult.success("Set player's armor");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set armor: " + e.getMessage());
        }
    }
}