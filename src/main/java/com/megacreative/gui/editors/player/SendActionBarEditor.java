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
import java.util.function.Consumer;

public class SendActionBarEditor extends AbstractParameterEditor {
    
    public SendActionBarEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Send Action Bar Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current message parameter value from the CodeBlock
        DataValue messageValue = codeBlock.getParameter("message");
        String currentMessage = (messageValue != null) ? messageValue.asString() : "Не задано";

        // Create an item to represent the message parameter
        ItemStack messageItem = new ItemStack(Material.PAPER);
        ItemMeta meta = messageItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aСообщение в ActionBar");
            meta.setLore(Arrays.asList(
                "§7Текущее: §f" + currentMessage, 
                "§eНажмите, чтобы изменить."
            ));
            messageItem.setItemMeta(meta);
        }

        // Place the item in the inventory
        inventory.setItem(0, messageItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Message slot
                openAnvilInputGUI();
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aSend Action Bar parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI() {
        // Get the current message value
        DataValue messageValue = codeBlock.getParameter("message");
        String currentMessage = (messageValue != null) ? messageValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newMessage) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("message", newMessage);
            player.sendMessage("§aAction Bar message saved!");
            // Reopen the editor to show the updated value
            populateItems();
        };
        
        // Create a cancel callback
        Runnable cancelCallback = () -> {
            // Reopen the editor
            populateItems();
            player.openInventory(inventory);
        };
        
        // Create and open the anvil input GUI
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Enter Action Bar message", callback, cancelCallback);
    }
}