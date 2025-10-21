package com.megacreative.events;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Custom event for when a world unloads
 * This event is fired when a world unloads, after being processed by our Bukkit listener
 */
public class MegaWorldUnloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final World world;
    
    public MegaWorldUnloadEvent(@NotNull World world) {
        this.world = world;
    }
    
    @NotNull
    public World getWorld() {
        return world;
    }
    
    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
    
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}