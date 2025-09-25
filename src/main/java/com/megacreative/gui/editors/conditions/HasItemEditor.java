package com.megacreative.gui.editors.conditions;

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
 * Parameter editor for the HasItem condition
 * Allows players to set the item and amount to check for
 */
public class HasItemEditor extends AbstractParameterEditor {

    /**
     * Constructor for HasItemEditor
     * @param plugin The main plugin instance
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     */
    public HasItemEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 27, "Настроить проверку предмета");
    }

    /**
     * Populates the inventory with items for configuring the item check
     */
    @Override
    public void populateItems() {
        // Get the current parameter values from the CodeBlock
        DataValue itemValue = codeBlock.getParameter("item");
        String item = (itemValue != null) ? itemValue.asString() : "Не задано";
        
        DataValue amountValue = codeBlock.getParameter("amount");
        String amount = (amountValue != null) ? amountValue.asString() : "1";

        // Create items for the parameters
        ItemStack itemItem = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = itemItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName("§aПредмет");
            itemMeta.setLore(Arrays.asList(
                "§7Текущий: §f" + item, 
                "§eНажмите, чтобы изменить."
            ));
            itemItem.setItemMeta(itemMeta);
        }

        ItemStack amountItem = new ItemStack(Material.HOPPER);
        ItemMeta amountMeta = amountItem.getItemMeta();
        if (amountMeta != null) {
            amountMeta.setDisplayName("§aКоличество");
            amountMeta.setLore(Arrays.asList(
                "§7Текущее: §f" + amount, 
                "§eНажмите, чтобы изменить."
            ));
            amountItem.setItemMeta(amountMeta);
        }

        // Place the items in the inventory
        inventory.setItem(11, itemItem);   // Item - left
        inventory.setItem(15, amountItem); // Amount - right
        
        // Add info item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§eИнформация");
            infoMeta.setLore(Arrays.asList(
                "§7Для предмета используйте названия материалов Bukkit",
                "§7Пример: DIAMOND, STONE, OAK_LOG",
                "§7Количество должно быть числом"
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
            case 11: // Item
                openAnvilInputGUI("item", "Введите название предмета");
                break;
            case 15: // Amount
                openAnvilInputGUI("amount", "Введите количество");
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
            new HasItemEditor(plugin, player, codeBlock).open();
        };
        
        // Create a cancel callback
        Runnable cancelCallback = () -> player.closeInventory();
        
        // Create and open the anvil input GUI
        new com.megacreative.gui.AnvilInputGUI(plugin, player, title, callback, cancelCallback);
    }
}