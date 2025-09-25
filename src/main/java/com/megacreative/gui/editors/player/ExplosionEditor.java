package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ExplosionEditor extends AbstractParameterEditor {
    
    public ExplosionEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Explosion Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Power slot
        ItemStack powerStack = new ItemStack(Material.TNT);
        inventory.setItem(0, powerStack);
        
        // Break blocks slot
        ItemStack breakBlocksStack = new ItemStack(Material.BARRIER);
        inventory.setItem(1, breakBlocksStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Power slot
                openAnvilInputGUI("Enter explosion power", codeBlock.getParameter("power", "4.0").toString(), 
                    newValue -> codeBlock.setParameter("power", newValue));
                break;
                
            case 1: // Break blocks slot
                openAnvilInputGUI("Enter break blocks (true/false)", codeBlock.getParameter("breakBlocks", "true").toString(), 
                    newValue -> codeBlock.setParameter("breakBlocks", newValue));
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aExplosion parameters saved!");
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