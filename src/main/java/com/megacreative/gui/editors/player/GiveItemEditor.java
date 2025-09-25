package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.function.Consumer;

public class GiveItemEditor extends AbstractParameterEditor {
    
    public GiveItemEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Give Item Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Item type slot
        ItemStack itemItem = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = itemItem.getItemMeta();
        itemMeta.setDisplayName("§eItem Type");
        DataValue item = codeBlock.getParameter("item", DataValue.of("DIAMOND"));
        itemMeta.setLore(java.util.Arrays.asList(
            "§7Enter the item type to give",
            "§aCurrent value: §f" + (item != null ? item.asString() : "DIAMOND")
        ));
        itemItem.setItemMeta(itemMeta);
        inventory.setItem(0, itemItem);
        
        // Amount slot
        ItemStack amountItem = new ItemStack(Material.HOPPER);
        ItemMeta amountMeta = amountItem.getItemMeta();
        amountMeta.setDisplayName("§eAmount");
        DataValue amount = codeBlock.getParameter("amount", DataValue.of("1"));
        amountMeta.setLore(java.util.Arrays.asList(
            "§7Enter the amount of items to give",
            "§aCurrent value: §f" + (amount != null ? amount.asString() : "1")
        ));
        amountItem.setItemMeta(amountMeta);
        inventory.setItem(1, amountItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Give Item action",
            "",
            "§eHow to use:",
            "§71. Set the item type to give",
            "§72. Set the amount of items to give"
        ));
        helpItem.setItemMeta(helpMeta);
        inventory.setItem(8, helpItem);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();
        
        switch (slot) {
            case 0: // Item type slot
                // Open anvil GUI for item type input
                DataValue currentItem = codeBlock.getParameter("item", DataValue.of("DIAMOND"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter item type", 
                    newValue -> {
                        codeBlock.setParameter("item", DataValue.of(newValue));
                        player.sendMessage("§aItem type set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Amount slot
                // Open anvil GUI for amount input
                DataValue currentAmount = codeBlock.getParameter("amount", DataValue.of("1"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter amount", 
                    newValue -> {
                        codeBlock.setParameter("amount", DataValue.of(newValue));
                        player.sendMessage("§aAmount set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Give Item action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}