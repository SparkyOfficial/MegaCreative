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

public class HealPlayerEditor extends AbstractParameterEditor {
    
    public HealPlayerEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Heal Player Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Heal amount slot
        ItemStack amountItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta amountMeta = amountItem.getItemMeta();
        amountMeta.setDisplayName("§eHeal Amount");
        DataValue amount = codeBlock.getParameter("amount", DataValue.of("10.0"));
        amountMeta.setLore(java.util.Arrays.asList(
            "§7Enter the amount of health to heal",
            "§aCurrent value: §f" + (amount != null ? amount.asString() : "10.0")
        ));
        amountItem.setItemMeta(amountMeta);
        inventory.setItem(0, amountItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Heal Player action",
            "",
            "§eHow to use:",
            "§71. Set the amount of health to heal"
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
            case 0: // Heal amount slot
                // Open anvil GUI for heal amount input
                DataValue currentAmount = codeBlock.getParameter("amount", DataValue.of("10.0"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter heal amount", 
                    newValue -> {
                        codeBlock.setParameter("amount", DataValue.of(newValue));
                        player.sendMessage("§aHeal amount set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Heal Player action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}