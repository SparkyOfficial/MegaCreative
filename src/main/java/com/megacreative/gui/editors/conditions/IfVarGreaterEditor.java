package com.megacreative.gui.editors.conditions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class IfVarGreaterEditor extends AbstractParameterEditor {
    
    public IfVarGreaterEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "If Variable Greater Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Variable slot
        ItemStack varStack = new ItemStack(Material.OBSIDIAN);
        inventory.setItem(0, varStack);
        
        // Value slot
        ItemStack valueStack = new ItemStack(Material.GOLD_INGOT);
        inventory.setItem(1, valueStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Variable slot
                openAnvilInputGUI("Enter variable name", codeBlock.getParameter("variable", "myVar").toString(), 
                    newValue -> codeBlock.setParameter("variable", newValue));
                break;
                
            case 1: // Value slot
                openAnvilInputGUI("Enter value", codeBlock.getParameter("value", "0").toString(), 
                    newValue -> codeBlock.setParameter("value", newValue));
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aIf Variable Greater parameters saved!");
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