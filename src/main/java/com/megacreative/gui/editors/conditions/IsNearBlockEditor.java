package com.megacreative.gui.editors.conditions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class IsNearBlockEditor extends AbstractParameterEditor {
    
    public IsNearBlockEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Is Near Block Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Block slot
        ItemStack blockStack = new ItemStack(Material.GLASS);
        inventory.setItem(0, blockStack);
        
        // Radius slot
        ItemStack radiusStack = new ItemStack(Material.COMPASS);
        inventory.setItem(1, radiusStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Block slot
                openAnvilInputGUI("Enter block type", codeBlock.getParameter("block", "STONE").toString(), 
                    newValue -> codeBlock.setParameter("block", newValue));
                break;
                
            case 1: // Radius slot
                openAnvilInputGUI("Enter radius", codeBlock.getParameter("radius", "5").toString(), 
                    newValue -> codeBlock.setParameter("radius", newValue));
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aIs Near Block parameters saved!");
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