package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaPlayerTeleportEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts PlayerTeleportEvent to MegaPlayerTeleportEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitPlayerTeleportListener implements Listener {
    private final Plugin plugin;
    
    public BukkitPlayerTeleportListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent bukkitEvent) {
        
        MegaPlayerTeleportEvent internalEvent = new MegaPlayerTeleportEvent(
            bukkitEvent.getPlayer(),
            bukkitEvent.getFrom(),
            bukkitEvent.getTo(),
            bukkitEvent.getCause().name()
        );
        
        
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}