package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

/**
 * Action that gives an item to the player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "giveItem", displayName = "Give Item", type = BlockType.ACTION)
public class GiveItemAction implements BlockAction {
    
    private static final Logger LOGGER = Logger.getLogger(GiveItemAction.class.getName());
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("No player in execution context");
            }
            
            // Get the item parameter
            DataValue itemValue = block.getParameter("item");
            if (itemValue == null || itemValue.isEmpty()) {
                return ExecutionResult.error("Missing item parameter");
            }
            
            // Parse the material
            Material material;
            try {
                material = Material.valueOf(itemValue.asString().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid item material: " + itemValue.asString());
            }
            
            // Get the amount parameter (default to 1)
            int amount = 1;
            DataValue amountValue = block.getParameter("amount");
            if (amountValue != null && !amountValue.isEmpty()) {
                try {
                    amount = Integer.parseInt(amountValue.asString());
                } catch (NumberFormatException e) {
                    return ExecutionResult.error("Invalid amount: " + amountValue.asString());
                }
            }
            
            // Create the item stack
            ItemStack itemStack = new ItemStack(material, amount);
            
            // Give the item to the player
            player.getInventory().addItem(itemStack);
            
            // Send a message to the player
            player.sendMessage("§aReceived " + amount + "x " + material.name().toLowerCase());
            
            LOGGER.fine("Gave " + amount + "x " + material.name() + " to player " + player.getName());
            return ExecutionResult.success("Item given successfully");
        } catch (Exception e) {
            LOGGER.severe("Error executing GiveItemAction: " + e.getMessage());
            return ExecutionResult.error("Failed to give item: " + e.getMessage());
        }
    }
}