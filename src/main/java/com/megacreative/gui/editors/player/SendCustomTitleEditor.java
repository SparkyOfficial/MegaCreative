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

public class SendCustomTitleEditor extends AbstractParameterEditor {
    
    public SendCustomTitleEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Send Custom Title Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current title parameter value from the CodeBlock
        DataValue titleValue = codeBlock.getParameter("title");
        String currentTitle = (titleValue != null) ? titleValue.asString() : "Не задано";
        
        // Get the current subtitle parameter value from the CodeBlock
        DataValue subtitleValue = codeBlock.getParameter("subtitle");
        String currentSubtitle = (subtitleValue != null) ? subtitleValue.asString() : "Не задано";
        
        // Get the current fadein parameter value from the CodeBlock
        DataValue fadeinValue = codeBlock.getParameter("fadein");
        String currentFadein = (fadeinValue != null) ? fadeinValue.asString() : "Не задано";
        
        // Get the current stay parameter value from the CodeBlock
        DataValue stayValue = codeBlock.getParameter("stay");
        String currentStay = (stayValue != null) ? stayValue.asString() : "Не задано";
        
        // Get the current fadeout parameter value from the CodeBlock
        DataValue fadeoutValue = codeBlock.getParameter("fadeout");
        String currentFadeout = (fadeoutValue != null) ? fadeoutValue.asString() : "Не задано";

        // Create items to represent the parameters
        ItemStack titleItem = new ItemStack(Material.PAPER);
        ItemMeta titleMeta = titleItem.getItemMeta();
        if (titleMeta != null) {
            titleMeta.setDisplayName("§aЗаголовок");
            titleMeta.setLore(Arrays.asList(
                "§7Текущий: §f" + currentTitle, 
                "§eНажмите, чтобы изменить."
            ));
            titleItem.setItemMeta(titleMeta);
        }
        
        ItemStack subtitleItem = new ItemStack(Material.PAPER);
        ItemMeta subtitleMeta = subtitleItem.getItemMeta();
        if (subtitleMeta != null) {
            subtitleMeta.setDisplayName("§bПодзаголовок");
            subtitleMeta.setLore(Arrays.asList(
                "§7Текущий: §f" + currentSubtitle, 
                "§eНажмите, чтобы изменить."
            ));
            subtitleItem.setItemMeta(subtitleMeta);
        }
        
        ItemStack fadeinItem = new ItemStack(Material.PAPER);
        ItemMeta fadeinMeta = fadeinItem.getItemMeta();
        if (fadeinMeta != null) {
            fadeinMeta.setDisplayName("§cВремя появления");
            fadeinMeta.setLore(Arrays.asList(
                "§7Текущее: §f" + currentFadein, 
                "§eНажмите, чтобы изменить."
            ));
            fadeinItem.setItemMeta(fadeinMeta);
        }
        
        ItemStack stayItem = new ItemStack(Material.PAPER);
        ItemMeta stayMeta = stayItem.getItemMeta();
        if (stayMeta != null) {
            stayMeta.setDisplayName("§dВремя отображения");
            stayMeta.setLore(Arrays.asList(
                "§7Текущее: §f" + currentStay, 
                "§eНажмите, чтобы изменить."
            ));
            stayItem.setItemMeta(stayMeta);
        }
        
        ItemStack fadeoutItem = new ItemStack(Material.PAPER);
        ItemMeta fadeoutMeta = fadeoutItem.getItemMeta();
        if (fadeoutMeta != null) {
            fadeoutMeta.setDisplayName("§eВремя исчезновения");
            fadeoutMeta.setLore(Arrays.asList(
                "§7Текущее: §f" + currentFadeout, 
                "§eНажмите, чтобы изменить."
            ));
            fadeoutItem.setItemMeta(fadeoutMeta);
        }

        // Place the items in the inventory
        inventory.setItem(0, titleItem);
        inventory.setItem(1, subtitleItem);
        inventory.setItem(2, fadeinItem);
        inventory.setItem(3, stayItem);
        inventory.setItem(4, fadeoutItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Title slot
                openAnvilInputGUI("title", "Enter title text");
                break;
                
            case 1: // Subtitle slot
                openAnvilInputGUI("subtitle", "Enter subtitle text");
                break;
                
            case 2: // Fadein slot
                openAnvilInputGUI("fadein", "Enter fadein time");
                break;
                
            case 3: // Stay slot
                openAnvilInputGUI("stay", "Enter stay time");
                break;
                
            case 4: // Fadeout slot
                openAnvilInputGUI("fadeout", "Enter fadeout time");
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aSend Custom Title parameters saved!");
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