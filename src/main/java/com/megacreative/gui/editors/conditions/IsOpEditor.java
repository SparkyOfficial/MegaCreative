package com.megacreative.gui.editors.conditions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class IsOpEditor extends AbstractParameterEditor {
    
    public IsOpEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Is Op Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Info slot
        ItemStack infoStack = new ItemStack(Material.COMMAND_BLOCK);
        inventory.setItem(0, infoStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aIs Op parameters saved!");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}