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
            // Clean up async loops for this player
            com.megacreative.coding.actions.advanced.AsyncLoopControl.cleanupPlayerLoops(event.getPlayer().getUniqueId());
            
            // Additional cleanup can be added here if needed
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Error during player quit cleanup: " + e.getMessage());
            }
        }
    }
}