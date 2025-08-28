package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

/**
 * Action that handles player entry events and can automatically give items
 * This action is specifically designed for the workflow: Entry -> Give Item
 */
public class PlayerEntryAction implements BlockAction {
    
    private static final Logger logger = Logger.getLogger(PlayerEntryAction.class.getName());
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) {
            logger.warning("Player or block is null in PlayerEntryAction");
            return;
        }
        
        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) {
            logger.warning("VariableManager is null in PlayerEntryAction");
            return;
        }
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
        // Check if this entry action should automatically give items
        DataValue autoGiveItem = block.getParameter("autoGiveItem");
        if (autoGiveItem != null && resolver.resolve(context, autoGiveItem).asBoolean()) {
            // Get item parameters
            DataValue rawItemName = block.getParameter("itemName");
            DataValue rawAmount = block.getParameter("itemAmount");
            
            if (rawItemName != null && rawAmount != null) {
                String itemName = resolver.resolve(context, rawItemName).asString();
                String amountStr = resolver.resolve(context, rawAmount).asString();
                
                try {
                    Material material = Material.valueOf(itemName.toUpperCase());
                    int amount = Integer.parseInt(amountStr);
                    
                    player.getInventory().addItem(new ItemStack(material, amount));
                    player.sendMessage("§a✓ Добро пожаловать! Вы получили " + amount + "x " + material.name());
                } catch (Exception e) {
                    player.sendMessage("§cОшибка при выдаче предмета: неверный материал или количество.");
                    logger.warning("Error giving item in PlayerEntryAction: " + e.getMessage());
                }
            } else {
                // Standard entry message when auto-give is enabled but no items configured
                player.sendMessage("§a✓ Добро пожаловать в мир творчества!");
            }
        } else {
            // Standard entry message
            player.sendMessage("§a✓ Добро пожаловать в мир творчества!");
        }
    }
}