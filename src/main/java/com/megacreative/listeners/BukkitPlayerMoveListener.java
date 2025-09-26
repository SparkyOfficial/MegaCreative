package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaPlayerMoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts PlayerMoveEvent to MegaPlayerMoveEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitPlayerMoveListener implements Listener {
    private final Plugin plugin;
    
    public BukkitPlayerMoveListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent bukkitEvent) {
        // Optimization: don't fire event for micro-movements
        if (bukkitEvent.getFrom().getBlockX() == bukkitEvent.getTo().getBlockX() && 
            bukkitEvent.getFrom().getBlockY() == bukkitEvent.getTo().getBlockY() && 
            bukkitEvent.getFrom().getBlockZ() == bukkitEvent.getTo().getBlockZ()) {
            return;
        }
        
        // Create our custom event
        MegaPlayerMoveEvent internalEvent = new MegaPlayerMoveEvent(
            bukkitEvent.getPlayer(),
            bukkitEvent.getFrom(),
            bukkitEvent.getTo()
        );
        
        // Publish it to our event system
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}