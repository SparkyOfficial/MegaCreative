package com.megacreative.gui.editors.conditions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerHealthEditor extends AbstractParameterEditor {
    
    public PlayerHealthEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Player Health Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Health slot
        ItemStack healthStack = new ItemStack(Material.GOLDEN_APPLE);
        inventory.setItem(0, healthStack);
        
        // Operator slot
        ItemStack operatorStack = new ItemStack(Material.COMPARATOR);
        inventory.setItem(1, operatorStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Health slot
                openAnvilInputGUI("Enter health value", codeBlock.getParameter("health", "10.0").toString(), 
                    newValue -> codeBlock.setParameter("health", newValue));
                break;
                
            case 1: // Operator slot
                openAnvilInputGUI("Enter operator", codeBlock.getParameter("operator", ">").toString(), 
                    newValue -> codeBlock.setParameter("operator", newValue));
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aPlayer Health parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI(String title, String currentValue, AnvilInputGUI.ValueConsumer onComplete) {
        new AnvilInputGUI(plugin, player, title, onComplete).open();
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}