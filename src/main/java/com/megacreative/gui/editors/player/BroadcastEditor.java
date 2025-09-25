package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BroadcastEditor extends AbstractParameterEditor {
    
    public BroadcastEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Broadcast Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Message slot
        ItemStack messageStack = new ItemStack(Material.BELL);
        inventory.setItem(0, messageStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Message slot
                openAnvilInputGUI("Enter broadcast message", codeBlock.getParameter("message", "Hello everyone!").toString(), 
                    newValue -> codeBlock.setParameter("message", newValue));
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aBroadcast parameters saved!");
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