package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaPlayerQuitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts PlayerQuitEvent to MegaPlayerQuitEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitPlayerQuitListener implements Listener {
    private final Plugin plugin;
    
    public BukkitPlayerQuitListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent bukkitEvent) {
        // Create our custom event
        MegaPlayerQuitEvent internalEvent = new MegaPlayerQuitEvent(
            bukkitEvent.getPlayer(),
            bukkitEvent.getQuitMessage()
        );
        
        // Publish it to our event system
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}