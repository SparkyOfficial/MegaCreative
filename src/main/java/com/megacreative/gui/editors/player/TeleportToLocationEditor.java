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

public class TeleportToLocationEditor extends AbstractParameterEditor {
    
    public TeleportToLocationEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Teleport To Location Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current location parameter value from the CodeBlock
        DataValue locationValue = codeBlock.getParameter("location");
        String currentLocation = (locationValue != null) ? locationValue.asString() : "Не задано";

        // Create an item to represent the location parameter
        ItemStack locationItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = locationItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aЛокация");
            meta.setLore(Arrays.asList(
                "§7Текущая: §f" + currentLocation, 
                "§eНажмите, чтобы изменить."
            ));
            locationItem.setItemMeta(meta);
        }

        // Place the item in the inventory
        inventory.setItem(0, locationItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Location slot
                openAnvilInputGUI();
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aTeleport To Location parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI() {
        // Get the current location value
        DataValue locationValue = codeBlock.getParameter("location");
        String currentLocation = (locationValue != null) ? locationValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newLocation) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("location", newLocation);
            player.sendMessage("§aLocation saved!");
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
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Enter location (x:y:z:world)", callback, cancelCallback);
    }
}