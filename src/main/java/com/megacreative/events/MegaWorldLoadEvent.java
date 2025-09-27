package com.megacreative.events;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for when a world loads
 * This event is fired when a world loads, after being processed by our Bukkit listener
 */
public class MegaWorldLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final World world;
    
    public MegaWorldLoadEvent(World world) {
        this.world = world;
    }
    
    public World getWorld() {
        return world;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}