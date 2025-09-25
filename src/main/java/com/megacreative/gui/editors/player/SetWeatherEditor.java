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

public class SetWeatherEditor extends AbstractParameterEditor {
    
    public SetWeatherEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Set Weather Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Weather slot
        ItemStack weatherItem = new ItemStack(Material.WATER_BUCKET);
        ItemMeta weatherMeta = weatherItem.getItemMeta();
        weatherMeta.setDisplayName("§eWeather Type");
        DataValue weather = codeBlock.getParameter("weather", DataValue.of("clear"));
        weatherMeta.setLore(java.util.Arrays.asList(
            "§7Enter the weather type (clear/rain/thunder)",
            "§aCurrent value: §f" + (weather != null ? weather.asString() : "clear")
        ));
        weatherItem.setItemMeta(weatherMeta);
        inventory.setItem(0, weatherItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Set Weather action",
            "",
            "§eHow to use:",
            "§71. Set the weather type (clear/rain/thunder)"
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
            case 0: // Weather slot
                // Open anvil GUI for weather input
                DataValue currentWeather = codeBlock.getParameter("weather", DataValue.of("clear"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter weather (clear/rain/thunder)", 
                    newValue -> {
                        codeBlock.setParameter("weather", DataValue.of(newValue));
                        player.sendMessage("§aWeather set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Set Weather action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}