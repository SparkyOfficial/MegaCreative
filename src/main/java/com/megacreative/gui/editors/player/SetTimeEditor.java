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

public class SetTimeEditor extends AbstractParameterEditor {
    
    public SetTimeEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Set Time Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Time value slot
        ItemStack timeItem = new ItemStack(Material.CLOCK);
        ItemMeta timeMeta = timeItem.getItemMeta();
        timeMeta.setDisplayName("§eTime Value");
        DataValue time = codeBlock.getParameter("time", DataValue.of("0"));
        timeMeta.setLore(java.util.Arrays.asList(
            "§7Enter the time value to set",
            "§aCurrent value: §f" + (time != null ? time.asString() : "0")
        ));
        timeItem.setItemMeta(timeMeta);
        inventory.setItem(0, timeItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Set Time action",
            "",
            "§eHow to use:",
            "§71. Set the time value to set"
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
            case 0: // Time value slot
                // Open anvil GUI for time value input
                DataValue currentTime = codeBlock.getParameter("time", DataValue.of("0"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter time", 
                    newValue -> {
                        codeBlock.setParameter("time", DataValue.of(newValue));
                        player.sendMessage("§aTime value set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Set Time action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}