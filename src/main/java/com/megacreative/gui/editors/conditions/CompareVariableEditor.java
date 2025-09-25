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
 * Parameter editor for the CompareVariable condition
 * Allows players to set the two variables and operator for comparison
 */
public class CompareVariableEditor extends AbstractParameterEditor {

    /**
     * Constructor for CompareVariableEditor
     * @param plugin The main plugin instance
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     */
    public CompareVariableEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 27, "Настроить сравнение переменных");
    }

    /**
     * Populates the inventory with items for configuring the variable comparison
     */
    @Override
    public void populateItems() {
        // Get the current parameter values from the CodeBlock
        DataValue var1Value = codeBlock.getParameter("var1");
        String var1 = (var1Value != null) ? var1Value.asString() : "Не задано";
        
        DataValue operatorValue = codeBlock.getParameter("operator");
        String operator = (operatorValue != null) ? operatorValue.asString() : "Не задано";
        
        DataValue var2Value = codeBlock.getParameter("var2");
        String var2 = (var2Value != null) ? var2Value.asString() : "Не задано";

        // Create items for the three parameters
        ItemStack var1Item = new ItemStack(Material.NAME_TAG);
        ItemMeta var1Meta = var1Item.getItemMeta();
        if (var1Meta != null) {
            var1Meta.setDisplayName("§aПервая переменная");
            var1Meta.setLore(Arrays.asList(
                "§7Текущая: §f" + var1, 
                "§eНажмите, чтобы изменить."
            ));
            var1Item.setItemMeta(var1Meta);
        }

        ItemStack operatorItem = new ItemStack(Material.REDSTONE);
        ItemMeta operatorMeta = operatorItem.getItemMeta();
        if (operatorMeta != null) {
            operatorMeta.setDisplayName("§aОператор");
            operatorMeta.setLore(Arrays.asList(
                "§7Текущий: §f" + operator, 
                "§eНажмите, чтобы изменить."
            ));
            operatorItem.setItemMeta(operatorMeta);
        }

        ItemStack var2Item = new ItemStack(Material.NAME_TAG);
        ItemMeta var2Meta = var2Item.getItemMeta();
        if (var2Meta != null) {
            var2Meta.setDisplayName("§aВторая переменная");
            var2Meta.setLore(Arrays.asList(
                "§7Текущая: §f" + var2, 
                "§eНажмите, чтобы изменить."
            ));
            var2Item.setItemMeta(var2Meta);
        }

        // Place the items in the inventory
        inventory.setItem(10, var1Item);   // First variable - left
        inventory.setItem(13, operatorItem); // Operator - center
        inventory.setItem(16, var2Item);   // Second variable - right
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
            case 10: // First variable
                openAnvilInputGUI("var1", "Введите имя первой переменной");
                break;
            case 13: // Operator
                openAnvilInputGUI("operator", "Введите оператор (==, !=, <, >, <=, >=)");
                break;
            case 16: // Second variable
                openAnvilInputGUI("var2", "Введите имя второй переменной");
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
            new CompareVariableEditor(plugin, player, codeBlock).open();
        };
        
        // Create a cancel callback
        Runnable cancelCallback = () -> player.closeInventory();
        
        // Create and open the anvil input GUI
        new com.megacreative.gui.AnvilInputGUI(plugin, player, title, callback, cancelCallback);
    }
}