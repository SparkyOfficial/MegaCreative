package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaPlayerChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts AsyncPlayerChatEvent to MegaPlayerChatEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitPlayerChatListener implements Listener {
    private final Plugin plugin;
    
    public BukkitPlayerChatListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent bukkitEvent) {
        // Create our custom event
        MegaPlayerChatEvent internalEvent = new MegaPlayerChatEvent(
            bukkitEvent.getPlayer(),
            bukkitEvent.getMessage()
        );
        
        // Publish it to our event system
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}