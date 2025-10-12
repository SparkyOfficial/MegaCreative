package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaEntityPickupItemEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts EntityPickupItemEvent to MegaEntityPickupItemEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitEntityPickupItemListener implements Listener {
    private final Plugin plugin;
    
    public BukkitEntityPickupItemListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent bukkitEvent) {
        
        MegaEntityPickupItemEvent internalEvent = new MegaEntityPickupItemEvent(
            (org.bukkit.entity.Player) bukkitEvent.getEntity(),
            bukkitEvent.getItem().getItemStack(),
            bukkitEvent.getItem().getItemStack().getAmount()
        );
        
        
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}