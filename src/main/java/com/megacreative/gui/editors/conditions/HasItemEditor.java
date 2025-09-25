package com.megacreative.gui.editors.conditions;

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

public class HasItemEditor extends AbstractParameterEditor {

    /**
     * Constructor for HasItemEditor
     * @param plugin The main plugin instance
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     */
    public HasItemEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Has Item Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Item type slot
        ItemStack itemItem = new ItemStack(Material.STICK);
        ItemMeta itemMeta = itemItem.getItemMeta();
        itemMeta.setDisplayName("§eItem Type");
        DataValue item = codeBlock.getParameter("item", DataValue.of("DIAMOND"));
        itemMeta.setLore(java.util.Arrays.asList(
            "§7Enter the item type to check",
            "§aCurrent value: §f" + (item != null ? item.asString() : "DIAMOND")
        ));
        itemItem.setItemMeta(itemMeta);
        inventory.setItem(0, itemItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Has Item condition",
            "",
            "§eHow to use:",
            "§71. Set the item type to check"
        ));
        helpItem.setItemMeta(helpMeta);
        inventory.setItem(8, helpItem);
    }
    
    /**
     * Handles clicks in the inventory
     * @param event The inventory click event
     */
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
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Has Item condition.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}