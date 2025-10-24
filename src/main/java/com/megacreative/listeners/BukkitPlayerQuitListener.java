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
        // PlayerQuitEvent is never null when this method is called according to Bukkit API
        try {
            // Perform cleanup operations when player disconnects
            // This might include saving player data, removing from active sessions, etc.
            
            // Currently just logging that the player quit
            // According to static analysis, plugin and event.getPlayer() are never null when this method is called
            plugin.getLogger().fine("Player " + event.getPlayer().getName() + " disconnected from the server");
        } catch (Exception e) {
            // According to static analysis, plugin is never null when this method is called
            plugin.getLogger().warning("Error during player quit cleanup: " + e.getMessage());
        }
    }
}