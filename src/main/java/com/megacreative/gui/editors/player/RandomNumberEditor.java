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

public class RandomNumberEditor extends AbstractParameterEditor {
    
    public RandomNumberEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Random Number Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current min parameter value from the CodeBlock
        DataValue minValue = codeBlock.getParameter("min");
        String currentMin = (minValue != null) ? minValue.asString() : "Не задано";
        
        // Get the current max parameter value from the CodeBlock
        DataValue maxValue = codeBlock.getParameter("max");
        String currentMax = (maxValue != null) ? maxValue.asString() : "Не задано";
        
        // Get the current variable parameter value from the CodeBlock
        DataValue variableValue = codeBlock.getParameter("variable");
        String currentVariable = (variableValue != null) ? variableValue.asString() : "Не задано";

        // Create items to represent the parameters
        ItemStack minItem = new ItemStack(Material.IRON_NUGGET);
        ItemMeta minMeta = minItem.getItemMeta();
        if (minMeta != null) {
            minMeta.setDisplayName("§aМинимальное значение");
            minMeta.setLore(Arrays.asList(
                "§7Текущее: §f" + currentMin, 
                "§eНажмите, чтобы изменить."
            ));
            minItem.setItemMeta(minMeta);
        }
        
        ItemStack maxItem = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta maxMeta = maxItem.getItemMeta();
        if (maxMeta != null) {
            maxMeta.setDisplayName("§aМаксимальное значение");
            maxMeta.setLore(Arrays.asList(
                "§7Текущее: §f" + currentMax, 
                "§eНажмите, чтобы изменить."
            ));
            maxItem.setItemMeta(maxMeta);
        }
        
        ItemStack variableItem = new ItemStack(Material.NAME_TAG);
        ItemMeta variableMeta = variableItem.getItemMeta();
        if (variableMeta != null) {
            variableMeta.setDisplayName("§aПеременная для сохранения");
            variableMeta.setLore(Arrays.asList(
                "§7Текущая: §f" + currentVariable, 
                "§eНажмите, чтобы изменить."
            ));
            variableItem.setItemMeta(variableMeta);
        }

        // Place the items in the inventory
        inventory.setItem(0, minItem);
        inventory.setItem(1, maxItem);
        inventory.setItem(2, variableItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Min slot
                openAnvilInputGUI("min", "Enter minimum value");
                break;
                
            case 1: // Max slot
                openAnvilInputGUI("max", "Enter maximum value");
                break;
                
            case 2: // Variable slot
                openAnvilInputGUI("variable", "Enter variable name");
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aRandom Number parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI(String parameter, String title) {
        // Get the current parameter value
        DataValue paramValue = codeBlock.getParameter(parameter);
        String currentParam = (paramValue != null) ? paramValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newParam) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter(parameter, newParam);
            player.sendMessage("§a" + parameter + " saved!");
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
        new com.megacreative.gui.AnvilInputGUI(plugin, player, title, callback, cancelCallback);
    }
}