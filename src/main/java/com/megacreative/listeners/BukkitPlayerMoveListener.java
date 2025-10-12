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
        
        if (bukkitEvent.getFrom().getBlockX() == bukkitEvent.getTo().getBlockX() && 
            bukkitEvent.getFrom().getBlockY() == bukkitEvent.getTo().getBlockY() && 
            bukkitEvent.getFrom().getBlockZ() == bukkitEvent.getTo().getBlockZ()) {
            return;
        }
        
        
        MegaPlayerMoveEvent internalEvent = new MegaPlayerMoveEvent(
            bukkitEvent.getPlayer(),
            bukkitEvent.getFrom(),
            bukkitEvent.getTo()
        );
        
        
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}