package com.megacreative.gui.editors.events;

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
 * Parameter editor for the PlayerJoin event
 * Allows players to set parameters for when a player joins the game
 */
public class PlayerJoinEventEditor extends AbstractParameterEditor {

    /**
     * Constructor for PlayerJoinEventEditor
     * @param plugin The main plugin instance
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     */
    public PlayerJoinEventEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 27, "Настроить событие входа");
    }

    /**
     * Populates the inventory with items for configuring the player join event
     */
    @Override
    public void populateItems() {
        // Get the current parameter values from the CodeBlock
        DataValue messageValue = codeBlock.getParameter("joinMessage");
        String joinMessage = (messageValue != null) ? messageValue.asString() : "Не задано";
        
        DataValue broadcastValue = codeBlock.getParameter("broadcast");
        String broadcast = (broadcastValue != null) ? broadcastValue.asString() : "false";

        // Create items for the parameters
        ItemStack messageItem = new ItemStack(Material.PAPER);
        ItemMeta messageMeta = messageItem.getItemMeta();
        if (messageMeta != null) {
            messageMeta.setDisplayName("§aСообщение при входе");
            messageMeta.setLore(Arrays.asList(
                "§7Текущее: §f" + joinMessage, 
                "§eНажмите, чтобы изменить."
            ));
            messageItem.setItemMeta(messageMeta);
        }

        ItemStack broadcastItem = new ItemStack(Material.BELL);
        ItemMeta broadcastMeta = broadcastItem.getItemMeta();
        if (broadcastMeta != null) {
            broadcastMeta.setDisplayName("§aОповещение всем");
            broadcastMeta.setLore(Arrays.asList(
                "§7Текущее: §f" + broadcast, 
                "§eНажмите, чтобы изменить."
            ));
            broadcastItem.setItemMeta(broadcastMeta);
        }

        // Place the items in the inventory
        inventory.setItem(11, messageItem);   // Join message - left
        inventory.setItem(15, broadcastItem); // Broadcast - right
        
        // Add info item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§eИнформация");
            infoMeta.setLore(Arrays.asList(
                "§7Сообщение при входе: сообщение, которое",
                "§7видит игрок при входе на сервер",
                "§7Оповещение всем: true/false - отправлять",
                "§7ли сообщение всем игрокам на сервере"
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoItem);
    }

    /**
     * Handles clicks in the inventory
     * @param event The inventory click event
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        switch (slot) {
            case 11: // Join message
                openAnvilInputGUI("joinMessage", "Введите сообщение при входе");
                break;
            case 15: // Broadcast
                openAnvilInputGUI("broadcast", "Введите true или false");
                break;
        }
    }
    
    /**
     * Opens an anvil GUI for entering a parameter value
     */
    private void openAnvilInputGUI(String paramName, String title) {
        // Get the current parameter value
        DataValue paramValue = codeBlock.getParameter(paramName);
        String currentParam = (paramValue != null) ? paramValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newParam) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter(paramName, DataValue.fromObject(newParam));
            player.sendMessage("§aПараметр " + paramName + " сохранен!");
            player.closeInventory();
            
            // Reopen the editor to show updated values
            new PlayerJoinEventEditor(plugin, player, codeBlock).open();
        };
        
        // Create a cancel callback
        Runnable cancelCallback = () -> player.closeInventory();
        
        // Create and open the anvil input GUI
        new com.megacreative.gui.AnvilInputGUI(plugin, player, title, callback, cancelCallback);
    }
}