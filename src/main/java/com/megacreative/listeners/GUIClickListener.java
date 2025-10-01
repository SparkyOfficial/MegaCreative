package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.GUIClickEvent;
import com.megacreative.coding.actions.gui.CreateMenuAction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener for GUI click events
 * Detects when players click on items in custom GUIs and fires custom events
 */
public class GUIClickListener implements Listener {
    private final Plugin plugin;
    
    // Track which inventories are custom GUIs
    private final Map<Inventory, String> guiInventories = new ConcurrentHashMap<>();
    
    public GUIClickListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        
        // Check if this is one of our custom GUIs
        String menuTitle = guiInventories.get(inventory);
        if (menuTitle != null) {
            // Cancel the event to prevent players from taking items
            event.setCancelled(true);
            
            // Get the clicked item
            ItemStack clickedItem = event.getCurrentItem();
            int slot = event.getSlot();
            String clickType = event.getClick().name();
            
            // Fire the custom GUI click event
            GUIClickEvent guiClickEvent = new GUIClickEvent(player, inventory, slot, clickedItem, clickType, menuTitle);
            plugin.getServer().getPluginManager().callEvent(guiClickEvent);
        }
    }
    
    /**
     * Register a GUI inventory so we can detect clicks on it
     * @param inventory The inventory to register
     * @param title The title of the menu
     */
    public void registerGUIInventory(Inventory inventory, String title) {
        guiInventories.put(inventory, title);
    }
    
    /**
     * Unregister a GUI inventory
     * @param inventory The inventory to unregister
     */
    public void unregisterGUIInventory(Inventory inventory) {
        guiInventories.remove(inventory);
    }
}