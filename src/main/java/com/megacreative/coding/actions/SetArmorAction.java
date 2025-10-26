package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Action to set armor items for a player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "setArmor", displayName = "§bSet Armor", type = BlockType.ACTION)
public class SetArmorAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue helmetValue = block.getParameter("helmet");
            DataValue chestplateValue = block.getParameter("chestplate");
            DataValue leggingsValue = block.getParameter("leggings");
            DataValue bootsValue = block.getParameter("boots");
            
            // Resolve parameters and set armor
            ParameterResolver resolver = new ParameterResolver(context);
            
            if (helmetValue != null) {
                DataValue resolvedHelmet = resolver.resolve(context, helmetValue);
                String helmetStr = resolvedHelmet.asString();
                Material helmetMaterial = Material.matchMaterial(helmetStr);
                if (helmetMaterial != null) {
                    player.getInventory().setHelmet(new ItemStack(helmetMaterial));
                }
            }
            
            if (chestplateValue != null) {
                DataValue resolvedChestplate = resolver.resolve(context, chestplateValue);
                String chestplateStr = resolvedChestplate.asString();
                Material chestplateMaterial = Material.matchMaterial(chestplateStr);
                if (chestplateMaterial != null) {
                    player.getInventory().setChestplate(new ItemStack(chestplateMaterial));
                }
            }
            
            if (leggingsValue != null) {
                DataValue resolvedLeggings = resolver.resolve(context, leggingsValue);
                String leggingsStr = resolvedLeggings.asString();
                Material leggingsMaterial = Material.matchMaterial(leggingsStr);
                if (leggingsMaterial != null) {
                    player.getInventory().setLeggings(new ItemStack(leggingsMaterial));
                }
            }
            
            if (bootsValue != null) {
                DataValue resolvedBoots = resolver.resolve(context, bootsValue);
                String bootsStr = resolvedBoots.asString();
                Material bootsMaterial = Material.matchMaterial(bootsStr);
                if (bootsMaterial != null) {
                    player.getInventory().setBoots(new ItemStack(bootsMaterial));
                }
            }
            
            return ExecutionResult.success("Set armor items");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set armor: " + e.getMessage());
        }
    }
}