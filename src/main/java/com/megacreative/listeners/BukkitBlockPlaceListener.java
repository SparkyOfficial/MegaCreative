package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaBlockPlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts BlockPlaceEvent to MegaBlockPlaceEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitBlockPlaceListener implements Listener {
    private final Plugin plugin;
    
    public BukkitBlockPlaceListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent bukkitEvent) {
        
        MegaBlockPlaceEvent internalEvent = new MegaBlockPlaceEvent(
            bukkitEvent.getPlayer(),
            bukkitEvent.getBlock().getLocation(),
            bukkitEvent.getBlock().getType()
        );
        
        
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}