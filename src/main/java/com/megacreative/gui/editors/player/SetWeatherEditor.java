package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SetWeatherEditor extends AbstractParameterEditor {
    
    public SetWeatherEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Set Weather Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Weather slot
        ItemStack weatherStack = new ItemStack(Material.WATER_BUCKET);
        inventory.setItem(0, weatherStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Weather slot
                openAnvilInputGUI("Enter weather (clear/rain/thunder)", codeBlock.getParameter("weather", "clear").toString(), 
                    newValue -> codeBlock.setParameter("weather", newValue));
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aSet Weather parameters saved!");
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