package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Custom event for when a block is broken
 * This event is fired when a block is broken, after being processed by our Bukkit listener
 */
public class MegaBlockBreakEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final Location location;
    private final Material material;
    
    public MegaBlockBreakEvent(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        this.player = player;
        this.location = location;
        this.material = material;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public Location getLocation() {
        return location;
    }
    
    @NotNull
    public Material getMaterial() {
        return material;
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