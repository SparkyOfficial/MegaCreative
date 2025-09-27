package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaEntityDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts EntityDamageEvent to MegaEntityDamageEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitEntityDamageListener implements Listener {
    private final Plugin plugin;
    
    public BukkitEntityDamageListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent bukkitEvent) {
        // Only handle player damage events
        if (!(bukkitEvent.getEntity() instanceof org.bukkit.entity.Player)) {
            return;
        }
        
        // Create our custom event
        MegaEntityDamageEvent internalEvent = new MegaEntityDamageEvent(
            (org.bukkit.entity.Player) bukkitEvent.getEntity(),
            bukkitEvent.getDamage(),
            bukkitEvent.getCause()
        );
        
        // Publish it to our event system
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}