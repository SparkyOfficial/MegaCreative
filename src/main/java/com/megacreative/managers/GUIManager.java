package com.megacreative.managers;

import com.megacreative.MegaCreative;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all GUI instances and their event handling
 */
public class GUIManager implements Listener {
    
    private final MegaCreative plugin;
    private final Map<UUID, Object> activeGUIs = new HashMap<>();
    private final Map<Inventory, Object> inventoryToGUI = new HashMap<>();
    
    public GUIManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Registers a GUI for a player
     */
    public void registerGUI(Player player, Object gui, Inventory inventory) {
        activeGUIs.put(player.getUniqueId(), gui);
        inventoryToGUI.put(inventory, gui);
    }
    
    /**
     * Unregisters a GUI for a player
     */
    public void unregisterGUI(Player player) {
        Object gui = activeGUIs.remove(player.getUniqueId());
        if (gui != null) {
            // Remove from inventory mapping
            inventoryToGUI.entrySet().removeIf(entry -> entry.getValue().equals(gui));
        }
    }
    
    /**
     * Gets the active GUI for a player
     */
    public Object getActiveGUI(Player player) {
        return activeGUIs.get(player.getUniqueId());
    }
    
    /**
     * Checks if a player has an active GUI
     */
    public boolean hasActiveGUI(Player player) {
        return activeGUIs.containsKey(player.getUniqueId());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Inventory inventory = event.getInventory();
        Object gui = inventoryToGUI.get(inventory);
        
        if (gui != null) {
            event.setCancelled(true);
            
            // Handle click based on GUI type
            if (gui instanceof com.megacreative.gui.MyWorldsGUI) {
                ((com.megacreative.gui.MyWorldsGUI) gui).onInventoryClick(event);
            } else if (gui instanceof com.megacreative.gui.WorldCreationGUI) {
                ((com.megacreative.gui.WorldCreationGUI) gui).onInventoryClick(event);
            } else if (gui instanceof com.megacreative.gui.WorldBrowserGUI) {
                ((com.megacreative.gui.WorldBrowserGUI) gui).onInventoryClick(event);
            } else if (gui instanceof com.megacreative.gui.WorldSettingsGUI) {
                ((com.megacreative.gui.WorldSettingsGUI) gui).onInventoryClick(event);
            } else if (gui instanceof com.megacreative.gui.ScriptsGUI) {
                ((com.megacreative.gui.ScriptsGUI) gui).onInventoryClick(event);
            } else if (gui instanceof com.megacreative.gui.TemplateBrowserGUI) {
                ((com.megacreative.gui.TemplateBrowserGUI) gui).onInventoryClick(event);
            } else if (gui instanceof com.megacreative.gui.WorldCommentsGUI) {
                ((com.megacreative.gui.WorldCommentsGUI) gui).onInventoryClick(event);
            }
            // Add more GUI types as needed
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        Inventory inventory = event.getInventory();
        Object gui = inventoryToGUI.get(inventory);
        
        if (gui != null) {
            unregisterGUI(player);
        }
    }
}