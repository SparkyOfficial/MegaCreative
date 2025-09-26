package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaPlayerJoinedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts PlayerJoinEvent to MegaPlayerJoinedEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitPlayerJoinListener implements Listener {
    private final Plugin plugin;
    
    public BukkitPlayerJoinListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent bukkitEvent) {
        // Create our custom event
        MegaPlayerJoinedEvent internalEvent = new MegaPlayerJoinedEvent(
            bukkitEvent.getPlayer(),
            !bukkitEvent.getPlayer().hasPlayedBefore()
        );
        
        // Publish it to our event system
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}