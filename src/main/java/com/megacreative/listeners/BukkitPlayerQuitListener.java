package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player quit events
 * Handles cleanup when players disconnect from the server
 */
public class BukkitPlayerQuitListener implements Listener {
    private final MegaCreative plugin;
    
    public BukkitPlayerQuitListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event == null || event.getPlayer() == null) return;
        
        try {
            // Condition event.getPlayer() == null is always false when reached
            // Removed redundant null check since we already check for null above
            
            // Perform cleanup operations when player disconnects
            // This might include saving player data, removing from active sessions, etc.
            
            // Currently just logging that the player quit
            if (plugin != null) {
                plugin.getLogger().info("Player " + event.getPlayer().getName() + " disconnected from the server");
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Error during player quit cleanup: " + e.getMessage());
            }
        }
    }
}