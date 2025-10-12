package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaPlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts PlayerDeathEvent to MegaPlayerDeathEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitPlayerDeathListener implements Listener {
    private final Plugin plugin;
    
    public BukkitPlayerDeathListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent bukkitEvent) {
        
        MegaPlayerDeathEvent internalEvent = new MegaPlayerDeathEvent(
            bukkitEvent.getEntity(),
            bukkitEvent.getDeathMessage()
        );
        
        
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}