package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaBlockBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts BlockBreakEvent to MegaBlockBreakEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitBlockBreakListener implements Listener {
    private final Plugin plugin;
    
    public BukkitBlockBreakListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent bukkitEvent) {
        // Create our custom event
        MegaBlockBreakEvent internalEvent = new MegaBlockBreakEvent(
            bukkitEvent.getPlayer(),
            bukkitEvent.getBlock().getLocation(),
            bukkitEvent.getBlock().getType()
        );
        
        // Publish it to our event system
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}