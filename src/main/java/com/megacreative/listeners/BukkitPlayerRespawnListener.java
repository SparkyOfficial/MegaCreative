package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaPlayerRespawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts PlayerRespawnEvent to MegaPlayerRespawnEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitPlayerRespawnListener implements Listener {
    private final Plugin plugin;
    
    public BukkitPlayerRespawnListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent bukkitEvent) {
        // Create our custom event
        MegaPlayerRespawnEvent internalEvent = new MegaPlayerRespawnEvent(
            bukkitEvent.getPlayer(),
            bukkitEvent.getRespawnLocation(),
            bukkitEvent.isBedSpawn()
        );
        
        // Publish it to our event system
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}