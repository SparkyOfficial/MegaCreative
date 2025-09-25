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
 * Parameter editor for the Command action
 * Allows players to set the command that will be executed
 */
public class CommandEditor extends AbstractParameterEditor {

    /**
     * Constructor for CommandEditor
     * @param plugin The main plugin instance
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     */
    public CommandEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Настроить команду");
    }

    /**
     * Populates the inventory with items for configuring the command
     */
    @Override
    public void populateItems() {
        // Get the current command parameter value from the CodeBlock
        DataValue commandValue = codeBlock.getParameter("command");
        String currentCommand = (commandValue != null) ? commandValue.asString() : "Не задано";

        // Create an item to represent the command parameter
        ItemStack commandItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = commandItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aКоманда");
            meta.setLore(Arrays.asList(
                "§7Текущая: §f" + currentCommand, 
                "§eНажмите, чтобы изменить."
            ));
            commandItem.setItemMeta(meta);
        }

        // Place the item in the center of the inventory
        inventory.setItem(4, commandItem);
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
     * Opens an anvil GUI for entering the command
     */
    private void openAnvilInputGUI() {
        // Get the current command value
        DataValue commandValue = codeBlock.getParameter("command");
        String currentCommand = (commandValue != null) ? commandValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newCommand) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("command", DataValue.fromObject(newCommand));
            player.sendMessage("§aКоманда сохранена!");
            player.closeInventory();
        };
        
        // Create a cancel callback
        Runnable cancelCallback = () -> player.closeInventory();
        
        // Create and open the anvil input GUI
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Введите команду", callback, cancelCallback);
    }
}