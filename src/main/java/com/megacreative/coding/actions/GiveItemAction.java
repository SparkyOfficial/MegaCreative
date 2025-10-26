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
 * Action to give an item to a player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "giveItem", displayName = "§bGive Item", type = BlockType.ACTION)
public class GiveItemAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue itemValue = block.getParameter("item");
            DataValue amountValue = block.getParameter("amount");
            
            if (itemValue == null) {
                return ExecutionResult.error("Missing required parameter: item");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedItem = resolver.resolve(context, itemValue);
            
            String itemStr = resolvedItem.asString();
            Material material = Material.matchMaterial(itemStr);
            
            if (material == null) {
                return ExecutionResult.error("Invalid item material: " + itemStr);
            }
            
            // Get amount (default to 1)
            int amount = 1;
            if (amountValue != null) {
                DataValue resolvedAmount = resolver.resolve(context, amountValue);
                amount = Math.max(1, resolvedAmount.asNumber().intValue());
            }
            
            // Create item stack
            ItemStack itemStack = new ItemStack(material, amount);
            
            // Give item to player
            player.getInventory().addItem(itemStack);
            
            return ExecutionResult.success("Gave " + amount + " " + material.name() + " to player");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to give item: " + e.getMessage());
        }
    }
}