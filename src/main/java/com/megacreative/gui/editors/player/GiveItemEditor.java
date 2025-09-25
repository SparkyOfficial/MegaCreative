package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GiveItemEditor extends AbstractParameterEditor {
    
    public GiveItemEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Give Item Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Item slot
        ItemStack itemStack = new ItemStack(Material.CHEST);
        inventory.setItem(0, itemStack);
        
        // Amount slot
        ItemStack amountStack = new ItemStack(Material.HOPPER);
        inventory.setItem(1, amountStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Item slot
                openAnvilInputGUI("Enter item type", codeBlock.getParameter("item", "DIAMOND").toString(), 
                    newValue -> codeBlock.setParameter("item", newValue));
                break;
                
            case 1: // Amount slot
                openAnvilInputGUI("Enter amount", codeBlock.getParameter("amount", "1").toString(), 
                    newValue -> codeBlock.setParameter("amount", newValue));
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aGive Item parameters saved!");
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