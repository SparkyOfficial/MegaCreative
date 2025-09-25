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

public class ExecuteAsyncCommandEditor extends AbstractParameterEditor {
    
    public ExecuteAsyncCommandEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Execute Async Command Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
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

        // Place the item in the inventory
        inventory.setItem(0, commandItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Command slot
                openAnvilInputGUI();
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aExecute Async Command parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI() {
        // Get the current command value
        DataValue commandValue = codeBlock.getParameter("command");
        String currentCommand = (commandValue != null) ? commandValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newCommand) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("command", newCommand);
            player.sendMessage("§aCommand saved!");
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
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Enter command to execute", callback, cancelCallback);
    }
}