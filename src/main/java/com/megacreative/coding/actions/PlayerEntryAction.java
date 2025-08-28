package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.coding.containers.BlockContainer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.logging.Logger;

/**
 * Action that handles player entry events and can automatically give items
 * This action is specifically designed for the workflow: Entry -> Give Item
 * Integrates with the container system where items are configured in chests above blocks
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
            // Try to get items from container first (the chest above the block)
            if (tryGiveItemsFromContainer(context, player)) {
                player.sendMessage("§a✓ Добро пожаловать! Вы получили предметы из конфигурации.");
                return;
            }
            
            // Fallback to parameter-based item giving
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
    
    /**
     * Tries to give items from the container (chest) above the block
     * This implements the workflow where players configure items in a chest above the action block
     */
    private boolean tryGiveItemsFromContainer(ExecutionContext context, Player player) {
        try {
            BlockContainerManager containerManager = context.getPlugin().getBlockContainerManager();
            if (containerManager == null) {
                return false;
            }
            
            BlockContainer container = containerManager.getContainer(context.getCurrentBlock().getLocation());
            if (container == null) {
                return false;
            }
            
            // Get items from the container inventory
            List<ItemStack> itemsToGive = getItemsFromContainer(container);
            if (itemsToGive.isEmpty()) {
                return false;
            }
            
            // Give all items to the player
            for (ItemStack item : itemsToGive) {
                if (item != null && item.getType() != Material.AIR) {
                    player.getInventory().addItem(item.clone());
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.warning("Error giving items from container in PlayerEntryAction: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extracts items from a container
     */
    private List<ItemStack> getItemsFromContainer(BlockContainer container) {
        // This would typically interact with the container's inventory
        // For now, we'll return an empty list as the actual implementation
        // would depend on how the container system stores item data
        return new java.util.ArrayList<>();
    }
}