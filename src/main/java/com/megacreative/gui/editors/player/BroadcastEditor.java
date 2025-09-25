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

public class BroadcastEditor extends AbstractParameterEditor {
    
    public BroadcastEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Broadcast Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Message slot
        ItemStack messageItem = new ItemStack(Material.BELL);
        ItemMeta messageMeta = messageItem.getItemMeta();
        messageMeta.setDisplayName("§eBroadcast Message");
        DataValue message = codeBlock.getParameter("message", DataValue.of("Hello everyone!"));
        messageMeta.setLore(java.util.Arrays.asList(
            "§7Enter the broadcast message",
            "§aCurrent value: §f" + (message != null ? message.asString() : "Hello everyone!")
        ));
        messageItem.setItemMeta(messageMeta);
        inventory.setItem(0, messageItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Broadcast action",
            "",
            "§eHow to use:",
            "§71. Set the broadcast message"
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
            case 0: // Message slot
                // Open anvil GUI for message input
                DataValue currentMessage = codeBlock.getParameter("message", DataValue.of("Hello everyone!"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter broadcast message", 
                    newValue -> {
                        codeBlock.setParameter("message", DataValue.of(newValue));
                        player.sendMessage("§aBroadcast message set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Broadcast action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}