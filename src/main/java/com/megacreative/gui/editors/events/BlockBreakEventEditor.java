package com.megacreative.gui.editors.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreakEventEditor extends AbstractParameterEditor {
    
    public BlockBreakEventEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Block Break Event Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Event info
        ItemStack infoStack = new ItemStack(Material.STONE);
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
                player.sendMessage("§aBlock Break Event parameters saved!");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}