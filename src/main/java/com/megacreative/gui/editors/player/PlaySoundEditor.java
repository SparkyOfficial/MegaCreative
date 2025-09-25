package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlaySoundEditor extends AbstractParameterEditor {
    
    public PlaySoundEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Play Sound Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Sound slot
        ItemStack soundStack = new ItemStack(Material.NOTE_BLOCK);
        inventory.setItem(0, soundStack);
        
        // Volume slot
        ItemStack volumeStack = new ItemStack(Material.SLIME_BLOCK);
        inventory.setItem(1, volumeStack);
        
        // Pitch slot
        ItemStack pitchStack = new ItemStack(Material.NOTE_BLOCK);
        inventory.setItem(2, pitchStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Sound slot
                openAnvilInputGUI("Enter sound name", codeBlock.getParameter("sound", "ENTITY_PLAYER_LEVELUP").toString(), 
                    newValue -> codeBlock.setParameter("sound", newValue));
                break;
                
            case 1: // Volume slot
                openAnvilInputGUI("Enter volume", codeBlock.getParameter("volume", "1.0").toString(), 
                    newValue -> codeBlock.setParameter("volume", newValue));
                break;
                
            case 2: // Pitch slot
                openAnvilInputGUI("Enter pitch", codeBlock.getParameter("pitch", "1.0").toString(), 
                    newValue -> codeBlock.setParameter("pitch", newValue));
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aPlay Sound parameters saved!");
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