package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SpawnMobEditor extends AbstractParameterEditor {
    
    public SpawnMobEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Spawn Mob Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Mob slot
        ItemStack mobStack = new ItemStack(Material.ZOMBIE_HEAD);
        inventory.setItem(0, mobStack);
        
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
            case 0: // Mob slot
                openAnvilInputGUI("Enter mob type", codeBlock.getParameter("mob", "ZOMBIE").toString(), 
                    newValue -> codeBlock.setParameter("mob", newValue));
                break;
                
            case 1: // Amount slot
                openAnvilInputGUI("Enter amount", codeBlock.getParameter("amount", "1").toString(), 
                    newValue -> codeBlock.setParameter("amount", newValue));
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aSpawn Mob parameters saved!");
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