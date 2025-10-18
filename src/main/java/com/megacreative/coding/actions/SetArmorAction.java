package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

/**
 * Action for setting a player's armor.
 * This action sets the player's armor pieces from the new parameter system.
 */
@BlockMeta(id = "setArmor", displayName = "§aSet Armor", type = BlockType.ACTION)
public class SetArmorAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue helmetValue = block.getParameter("helmet");
            DataValue chestplateValue = block.getParameter("chestplate");
            DataValue leggingsValue = block.getParameter("leggings");
            DataValue bootsValue = block.getParameter("boots");
            
            
            if (helmetValue != null && !helmetValue.isEmpty()) {
                ItemStack helmet = parseItem(helmetValue);
                if (helmet != null) {
                    player.getInventory().setHelmet(helmet);
                }
            }
            
            if (chestplateValue != null && !chestplateValue.isEmpty()) {
                ItemStack chestplate = parseItem(chestplateValue);
                if (chestplate != null) {
                    player.getInventory().setChestplate(chestplate);
                }
            }
            
            if (leggingsValue != null && !leggingsValue.isEmpty()) {
                ItemStack leggings = parseItem(leggingsValue);
                if (leggings != null) {
                    player.getInventory().setLeggings(leggings);
                }
            }
            
            if (bootsValue != null && !bootsValue.isEmpty()) {
                ItemStack boots = parseItem(bootsValue);
                if (boots != null) {
                    player.getInventory().setBoots(boots);
                }
            }
            
            return ExecutionResult.success("Set player's armor");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set armor: " + e.getMessage());
        }
    }
    
    /**
     * Parses an item from a DataValue
     */
    private ItemStack parseItem(DataValue itemValue) {
        try {
            if (itemValue == null || itemValue.isEmpty()) {
                return null;
            }
            
            String itemStr = itemValue.asString();
            if (itemStr == null || itemStr.isEmpty()) {
                return null;
            }
            
            
            String[] parts = itemStr.split(":");
            Material material = Material.valueOf(parts[0].toUpperCase());
            
            int amount = 1;
            if (parts.length > 1) {
                try {
                    amount = Integer.parseInt(parts[1]);
                    amount = Math.max(1, Math.min(64, amount)); 
                } catch (NumberFormatException e) {
                    // Log exception and continue processing
                    // This is expected behavior when parsing user input
                    // Use default amount when parsing fails
                }
            }
            
            return new ItemStack(material, amount);
        } catch (Exception e) {
            return null;
        }
    }
}