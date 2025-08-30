package com.megacreative.managers;

import com.megacreative.MegaCreative;
import java.util.ArrayList;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.interfaces.IPlayerManager;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;
import java.util.logging.Logger;
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
public class GUIManager implements Listener {
    private static final Logger log = Logger.getLogger(GUIManager.class.getName());
    
    private final MegaCreative plugin;
    private final IPlayerManager playerManager;
    private final VariableManager variableManager;
    
    // Thread-safe maps to prevent memory leaks
    private final Map<UUID, ManagedGUI> activeGUIs = new ConcurrentHashMap<>();
    private final Map<Inventory, ManagedGUI> inventoryToGUI = new ConcurrentHashMap<>();
    
    // Player metadata storage
    private final Map<UUID, Map<String, Object>> playerMetadata = new ConcurrentHashMap<>();
    
    /**
     * Constructor with required dependencies
     * @param playerManager The player manager instance
     * @param variableManager The variable manager instance
     */
    public GUIManager(IPlayerManager playerManager, VariableManager variableManager) {
        this.plugin = MegaCreative.getInstance();
        this.playerManager = playerManager;
        this.variableManager = variableManager;
    }
    
    /**
     * Shuts down the GUIManager and cleans up resources
     */
    public void shutdown() {
        // Close all open GUIs
        for (UUID playerId : new ArrayList<>(activeGUIs.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.closeInventory();
            }
        }
        
        // Clear all references
        activeGUIs.clear();
        inventoryToGUI.clear();
        playerMetadata.clear();
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
    
    /**
     * Sets metadata for a player
     */
    public <T> void setPlayerMetadata(Player player, String key, T value) {
        UUID playerId = player.getUniqueId();
        playerMetadata.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(key, value);
    }
    
    /**
     * Gets metadata for a player
     */
    @SuppressWarnings("unchecked")
    public <T> T getPlayerMetadata(Player player, String key, Class<T> type) {
        UUID playerId = player.getUniqueId();
        Map<String, Object> metadata = playerMetadata.get(playerId);
        if (metadata == null) {
            return null;
        }
        
        Object value = metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        
        return null;
    }
    
    /**
     * Removes metadata for a player
     */
    public void removePlayerMetadata(Player player, String key) {
        UUID playerId = player.getUniqueId();
        Map<String, Object> metadata = playerMetadata.get(playerId);
        if (metadata != null) {
            metadata.remove(key);
        }
    }
    
    /**
     * Clears all metadata for a player
     */
    public void clearPlayerMetadata(Player player) {
        UUID playerId = player.getUniqueId();
        playerMetadata.remove(playerId);
    }
    
    // === Методы для работы с подтверждением удаления мира ===
    
    /**
     * Устанавливает флаг ожидания подтверждения удаления мира
     */
    public void setAwaitingDeleteConfirmation(Player player, String worldId) {
        setPlayerMetadata(player, "delete_confirmation_world_id", worldId);
        setPlayerMetadata(player, "awaiting_delete_confirmation", true);
    }
    
    /**
     * Получает ID мира для подтверждения удаления
     */
    public String getDeleteConfirmationWorldId(Player player) {
        return getPlayerMetadata(player, "delete_confirmation_world_id", String.class);
    }
    
    /**
     * Проверяет, ожидает ли игрок подтверждения удаления мира
     */
    public boolean isAwaitingDeleteConfirmation(Player player) {
        return Boolean.TRUE.equals(getPlayerMetadata(player, "awaiting_delete_confirmation", Boolean.class));
    }
    
    /**
     * Очищает данные подтверждения удаления мира
     */
    public void clearDeleteConfirmation(Player player) {
        removePlayerMetadata(player, "delete_confirmation_world_id");
        removePlayerMetadata(player, "awaiting_delete_confirmation");
    }
    
    // === Методы для работы с вводом комментариев ===
    
    /**
     * Устанавливает флаг ожидания ввода комментария
     */
    public void setAwaitingCommentInput(Player player, String worldId) {
        setPlayerMetadata(player, "comment_input_world_id", worldId);
        setPlayerMetadata(player, "awaiting_comment_input", true);
    }
    
    /**
     * Получает ID мира для ввода комментария
     */
    public String getCommentInputWorldId(Player player) {
        return getPlayerMetadata(player, "comment_input_world_id", String.class);
    }
    
    /**
     * Проверяет, ожидает ли игрок ввода комментария
     */
    public boolean isAwaitingCommentInput(Player player) {
        return Boolean.TRUE.equals(getPlayerMetadata(player, "awaiting_comment_input", Boolean.class));
    }
    
    /**
     * Очищает данные ввода комментария
     */
    public void clearCommentInput(Player player) {
        removePlayerMetadata(player, "comment_input_world_id");
        removePlayerMetadata(player, "awaiting_comment_input");
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
        clearPlayerMetadata(event.getPlayer());
    }
    
    public void cleanup() {
        activeGUIs.clear();
        inventoryToGUI.clear();
        playerMetadata.clear();
    }
}