package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class ElseEditor extends AbstractParameterEditor {
    
    public ElseEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Else Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Create an info item explaining the else block
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§eElse Block Information");
            infoMeta.setLore(Arrays.asList(
                "§7This block executes when the",
                "§7previous condition is false.",
                "",
                "§7No parameters needed - just",
                "§7place it after an if block."
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        ItemMeta doneMeta = doneStack.getItemMeta();
        if (doneMeta != null) {
            doneMeta.setDisplayName("§aDone");
            doneMeta.setLore(Arrays.asList("§7Click to close editor"));
            doneStack.setItemMeta(doneMeta);
        }
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();
        
        switch (slot) {
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aElse block configured!");
                break;
                
            default:
                // Show info for any other slot clicked
                player.sendMessage("§eThis is an else block. It executes when the previous condition is false.");
                break;
        }
    }
    
    @Override
    protected void onLoadContainerItems(org.bukkit.inventory.Inventory containerInventory) {
        // Else blocks don't have configurable parameters, so nothing to load
    }
    
    @Override
    protected void onSaveContainerItems(org.bukkit.inventory.Inventory containerInventory) {
        // Else blocks don't have configurable parameters, so nothing to save
    }
}