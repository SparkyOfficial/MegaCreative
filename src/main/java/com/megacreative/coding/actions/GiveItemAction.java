package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

/**
 * Action for giving an item to a player.
 * This action gives an item to the player based on parameters.
 */
@BlockMeta(id = "giveItem", displayName = "§aGive Item", type = BlockType.ACTION)
public class GiveItemAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue materialValue = block.getParameter("material");
            DataValue amountValue = block.getParameter("amount");
            
            if (materialValue == null || materialValue.isEmpty()) {
                return ExecutionResult.error("Material parameter is missing");
            }
            
            
            Material material = Material.valueOf(materialValue.asString().toUpperCase());
            
            
            int amount = 1;
            if (amountValue != null && !amountValue.isEmpty()) {
                try {
                    amount = Integer.parseInt(amountValue.asString());
                    amount = Math.max(1, Math.min(64, amount)); 
                } catch (NumberFormatException e) {
                    
                }
            }
            
            
            ItemStack item = new ItemStack(material, amount);
            player.getInventory().addItem(item);
            
            return ExecutionResult.success("Item given to player successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to give item: " + e.getMessage());
        }
    }
}