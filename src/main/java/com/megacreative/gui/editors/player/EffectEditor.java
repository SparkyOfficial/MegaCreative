package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EffectEditor extends AbstractParameterEditor {
    
    public EffectEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Effect Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Effect slot
        ItemStack effectStack = new ItemStack(Material.POTION);
        inventory.setItem(0, effectStack);
        
        // Duration slot
        ItemStack durationStack = new ItemStack(Material.CLOCK);
        inventory.setItem(1, durationStack);
        
        // Amplifier slot
        ItemStack amplifierStack = new ItemStack(Material.REDSTONE);
        inventory.setItem(2, amplifierStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Effect slot
                openAnvilInputGUI("Enter effect name", codeBlock.getParameter("effect", "SPEED").toString(), 
                    newValue -> codeBlock.setParameter("effect", newValue));
                break;
                
            case 1: // Duration slot
                openAnvilInputGUI("Enter duration", codeBlock.getParameter("duration", "200").toString(), 
                    newValue -> codeBlock.setParameter("duration", newValue));
                break;
                
            case 2: // Amplifier slot
                openAnvilInputGUI("Enter amplifier", codeBlock.getParameter("amplifier", "0").toString(), 
                    newValue -> codeBlock.setParameter("amplifier", newValue));
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aEffect parameters saved!");
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