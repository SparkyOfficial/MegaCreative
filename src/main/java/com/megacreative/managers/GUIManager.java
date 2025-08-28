package com.megacreative.managers;

import com.megacreative.interfaces.IPlayerManager;
import com.megacreative.managers.DataManager;
import lombok.extern.java.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced GUI Manager with memory leak prevention and proper event handling
 */
@Log
public class GUIManager implements Listener {
    
    private final IPlayerManager playerManager;
    private final DataManager dataManager;
    
    // Thread-safe maps to prevent memory leaks
    private final Map<UUID, ManagedGUI> activeGUIs = new ConcurrentHashMap<>();
    private final Map<Inventory, ManagedGUI> inventoryToGUI = new ConcurrentHashMap<>();
    
    /**
     * Constructor with specific dependencies
     */
    public GUIManager(IPlayerManager playerManager, DataManager dataManager) {
        this.playerManager = playerManager;
        this.dataManager = dataManager;
    }
    
    /**
     * Legacy constructor for backward compatibility
     */
    @Deprecated
    public GUIManager(com.megacreative.MegaCreative plugin) {
        this.playerManager = plugin.getPlayerManager();
        this.dataManager = plugin.getDataManager();
    }
    
    /**
     * Interface for manageable GUIs
     */
    public interface ManagedGUIInterface {
        /**
         * Handles inventory click events
         */
        void onInventoryClick(InventoryClickEvent event);
        
        /**
         * Handles inventory close events
         */
        default void onInventoryClose(InventoryCloseEvent event) {
            // Default implementation - override if needed
        }
        
        /**
         * Called when GUI is being cleaned up
         */
        default void onCleanup() {
            // Default implementation - override if needed
        }
        
        /**
         * Gets the GUI title for debugging
         */
        default String getGUITitle() {
            return "Unknown GUI";
        }
    }
    
    /**
     * Wrapper for GUI objects with metadata
     */
    private static class ManagedGUI {
        private final ManagedGUIInterface gui;
        private final long createdTime;
        private long lastAccessTime;
        private final String title;
        
        public ManagedGUI(ManagedGUIInterface gui) {
            this.gui = gui;
            this.createdTime = System.currentTimeMillis();
            this.lastAccessTime = createdTime;
            this.title = gui.getGUITitle();
        }
        
        public ManagedGUIInterface getGUI() {
            updateLastAccess();
            return gui;
        }
        
        public void updateLastAccess() {
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        public void cleanup() {
            gui.onCleanup();
        }
        
        // Getters
        public long getCreatedTime() { return createdTime; }
        public long getLastAccessTime() { return lastAccessTime; }
        public String getTitle() { return title; }
    }
    
    /**
     * Registers a GUI for a player with proper memory management
     */
    public void registerGUI(Player player, ManagedGUIInterface gui, Inventory inventory) {
        UUID playerId = player.getUniqueId();
        
        // Clean up any existing GUI for this player
        unregisterGUI(player);
        
        ManagedGUI managedGUI = new ManagedGUI(gui);
        activeGUIs.put(playerId, managedGUI);
        inventoryToGUI.put(inventory, managedGUI);
        
        log.fine("Registered GUI: " + gui.getGUITitle() + " for player: " + player.getName());
    }
    
    /**
     * Unregisters a GUI for a player with proper cleanup
     */
    public void unregisterGUI(Player player) {
        UUID playerId = player.getUniqueId();
        ManagedGUI managedGUI = activeGUIs.remove(playerId);
        
        if (managedGUI != null) {
            // Remove from inventory mapping
            inventoryToGUI.entrySet().removeIf(entry -> entry.getValue().equals(managedGUI));
            
            // Call cleanup
            managedGUI.cleanup();
            
            log.fine("Unregistered GUI: " + managedGUI.getTitle() + " for player: " + player.getName());
        }
    }
    
    /**
     * Gets the active GUI for a player
     */
    public ManagedGUIInterface getActiveGUI(Player player) {
        ManagedGUI managedGUI = activeGUIs.get(player.getUniqueId());
        return managedGUI != null ? managedGUI.getGUI() : null;
    }
    
    /**
     * Checks if a player has an active GUI
     */
    public boolean hasActiveGUI(Player player) {
        return activeGUIs.containsKey(player.getUniqueId());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ManagedGUI managedGUI = inventoryToGUI.get(event.getInventory());
        
        if (managedGUI != null) {
            managedGUI.getGUI().onInventoryClick(event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        ManagedGUI managedGUI = inventoryToGUI.get(event.getInventory());
        
        if (managedGUI != null) {
            managedGUI.getGUI().onInventoryClose(event);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        unregisterGUI(event.getPlayer());
    }
    
    public void cleanup() {
        activeGUIs.clear();
        inventoryToGUI.clear();
    }
}