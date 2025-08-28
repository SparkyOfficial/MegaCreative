package com.megacreative.managers;

import com.megacreative.interfaces.IPlayerManager;
import lombok.extern.java.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

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
    
    // Cleanup task to prevent memory leaks
    private BukkitTask cleanupTask;
    private static final long CLEANUP_INTERVAL = 6000L; // 5 minutes
    private static final long GUI_TIMEOUT = 1800000L; // 30 minutes
    
    /**
     * Constructor with specific dependencies (no God Object)
     */
    public GUIManager(IPlayerManager playerManager, DataManager dataManager) {
        this.playerManager = playerManager;
        this.dataManager = dataManager;
        startCleanupTask();
    }
    
    /**
     * Legacy constructor for backward compatibility
     * @deprecated Use constructor with specific dependencies
     */
    @Deprecated
    public GUIManager(com.megacreative.MegaCreative plugin) {
        this.playerManager = plugin.getPlayerManager();
        this.dataManager = plugin.getDataManager();
        startCleanupTask();
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
        
        public boolean isExpired() {
            return (System.currentTimeMillis() - lastAccessTime) > GUI_TIMEOUT;
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
     * Registers a legacy GUI object (with adapter)
     */
    @Deprecated
    public void registerGUI(Player player, Object legacyGUI, Inventory inventory) {
        ManagedGUIInterface adaptedGUI = createLegacyAdapter(legacyGUI);
        registerGUI(player, adaptedGUI, inventory);
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
     * Gets GUI statistics for monitoring
     */
    public GUIStatistics getStatistics() {
        int totalGUIs = activeGUIs.size();
        int expiredGUIs = 0;
        long oldestGUI = System.currentTimeMillis();
        
        for (ManagedGUI gui : activeGUIs.values()) {
            if (gui.isExpired()) expiredGUIs++;
            oldestGUI = Math.min(oldestGUI, gui.getCreatedTime());
        }
        
        return new GUIStatistics(totalGUIs, expiredGUIs, oldestGUI);
    }
    
    // Event Handlers
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Inventory inventory = event.getInventory();
        ManagedGUI managedGUI = inventoryToGUI.get(inventory);
        
        if (managedGUI != null) {
            event.setCancelled(true);
            
            try {
                managedGUI.getGUI().onInventoryClick(event);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error handling GUI click for " + managedGUI.getTitle(), e);
                // Unregister problematic GUI
                unregisterGUI(player);
                player.closeInventory();
                player.sendMessage("§cГUI error - interface closed");
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        Inventory inventory = event.getInventory();
        ManagedGUI managedGUI = inventoryToGUI.get(inventory);
        
        if (managedGUI != null) {
            try {
                managedGUI.getGUI().onInventoryClose(event);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error handling GUI close for " + managedGUI.getTitle(), e);
            } finally {
                unregisterGUI(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up GUIs when player disconnects to prevent memory leaks
        unregisterGUI(event.getPlayer());
    }
    
    // Utility Methods
    
    /**
     * Starts the cleanup task to prevent memory leaks
     */
    private void startCleanupTask() {
        if (cleanupTask != null && !cleanupTask.isCancelled()) {
            cleanupTask.cancel();
        }
        
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredGUIs();
            }
        }.runTaskTimerAsynchronously(org.bukkit.Bukkit.getPluginManager().getPlugins()[0], 
                                     CLEANUP_INTERVAL, CLEANUP_INTERVAL);
        
        log.fine("Started GUI cleanup task (interval: " + (CLEANUP_INTERVAL/20) + "s)");
    }
    
    /**
     * Cleans up expired GUIs to prevent memory leaks
     */
    private void cleanupExpiredGUIs() {
        int cleanedCount = 0;
        
        // Find expired GUIs
        activeGUIs.entrySet().removeIf(entry -> {
            ManagedGUI managedGUI = entry.getValue();
            if (managedGUI.isExpired()) {
                // Remove from inventory mapping as well
                inventoryToGUI.entrySet().removeIf(invEntry -> 
                    invEntry.getValue().equals(managedGUI));
                
                // Call cleanup
                managedGUI.cleanup();
                
                return true;
            }
            return false;
        });
        
        if (cleanedCount > 0) {
            log.info("Cleaned up " + cleanedCount + " expired GUIs");
        }
    }
    
    /**
     * Creates an adapter for legacy GUI objects
     */
    private ManagedGUIInterface createLegacyAdapter(Object legacyGUI) {
        return new ManagedGUIInterface() {
            @Override
            public void onInventoryClick(InventoryClickEvent event) {
                // Legacy GUI handling with instanceof checks
                if (legacyGUI instanceof com.megacreative.gui.MyWorldsGUI) {
                    ((com.megacreative.gui.MyWorldsGUI) legacyGUI).onInventoryClick(event);
                } else if (legacyGUI instanceof com.megacreative.gui.WorldCreationGUI) {
                    ((com.megacreative.gui.WorldCreationGUI) legacyGUI).onInventoryClick(event);
                } else if (legacyGUI instanceof com.megacreative.gui.WorldBrowserGUI) {
                    ((com.megacreative.gui.WorldBrowserGUI) legacyGUI).onInventoryClick(event);
                } else if (legacyGUI instanceof com.megacreative.gui.WorldSettingsGUI) {
                    ((com.megacreative.gui.WorldSettingsGUI) legacyGUI).onInventoryClick(event);
                } else if (legacyGUI instanceof com.megacreative.gui.ScriptsGUI) {
                    ((com.megacreative.gui.ScriptsGUI) legacyGUI).onInventoryClick(event);
                } else if (legacyGUI instanceof com.megacreative.gui.TemplateBrowserGUI) {
                    ((com.megacreative.gui.TemplateBrowserGUI) legacyGUI).onInventoryClick(event);
                } else if (legacyGUI instanceof com.megacreative.gui.WorldCommentsGUI) {
                    ((com.megacreative.gui.WorldCommentsGUI) legacyGUI).onInventoryClick(event);
                }
            }
            
            @Override
            public String getGUITitle() {
                return "Legacy GUI: " + legacyGUI.getClass().getSimpleName();
            }
        };
    }
    
    /**
     * Shutdown method to clean up resources
     */
    public void shutdown() {
        if (cleanupTask != null && !cleanupTask.isCancelled()) {
            cleanupTask.cancel();
        }
        
        // Clean up all GUIs
        activeGUIs.values().forEach(ManagedGUI::cleanup);
        activeGUIs.clear();
        inventoryToGUI.clear();
        
        log.info("GUIManager shut down successfully");
    }
    
    /**
     * Statistics class for monitoring GUI usage
     */
    public static class GUIStatistics {
        private final int totalGUIs;
        private final int expiredGUIs;
        private final long oldestGUITime;
        
        public GUIStatistics(int totalGUIs, int expiredGUIs, long oldestGUITime) {
            this.totalGUIs = totalGUIs;
            this.expiredGUIs = expiredGUIs;
            this.oldestGUITime = oldestGUITime;
        }
        
        public int getTotalGUIs() { return totalGUIs; }
        public int getExpiredGUIs() { return expiredGUIs; }
        public long getOldestGUITime() { return oldestGUITime; }
        public int getActiveGUIs() { return totalGUIs - expiredGUIs; }
        
        @Override
        public String toString() {
            return String.format("GUIStats{total=%d, active=%d, expired=%d}", 
                totalGUIs, getActiveGUIs(), expiredGUIs);
        }
    }
}