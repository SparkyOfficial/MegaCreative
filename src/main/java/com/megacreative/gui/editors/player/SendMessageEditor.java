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

/**
 * Parameter editor for the SendMessage action
 * Allows players to set the message text that will be sent to the player
 */
public class SendMessageEditor extends AbstractParameterEditor {

    /**
     * Constructor for SendMessageEditor
     * @param plugin The main plugin instance
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     */
    public SendMessageEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Настроить сообщение");
    }

    /**
     * Populates the inventory with items for configuring the message
     */
    @Override
    public void populateItems() {
        // Get the current message parameter value from the CodeBlock
        DataValue messageValue = codeBlock.getParameter("message");
        String currentMessage = (messageValue != null) ? messageValue.asString() : "Не задано";

        // Create an item to represent the message parameter
        ItemStack messageItem = new ItemStack(Material.PAPER);
        ItemMeta meta = messageItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aТекст сообщения");
            meta.setLore(Arrays.asList(
                "§7Текущее: §f" + currentMessage, 
                "§eНажмите, чтобы изменить."
            ));
            messageItem.setItemMeta(meta);
        }

        // Place the item in the center of the inventory
        inventory.setItem(4, messageItem);
    }

    /**
     * Handles clicks in the inventory
     * @param event The inventory click event
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        if (slot == 4) {
            // Open an anvil GUI for text input
            openAnvilInputGUI();
        }
    }
    
    /**
     * Opens an anvil GUI for entering the message text
     */
    private void openAnvilInputGUI() {
        // Get the current message value
        DataValue messageValue = codeBlock.getParameter("message");
        String currentMessage = (messageValue != null) ? messageValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newMessage) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("message", DataValue.fromObject(newMessage));
            player.sendMessage("§aСообщение сохранено!");
            player.closeInventory();
        };
        
        // Create a cancel callback
        Runnable cancelCallback = () -> player.closeInventory();
        
        // Create and open the anvil input GUI
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Введите сообщение", callback, cancelCallback);
    }
}