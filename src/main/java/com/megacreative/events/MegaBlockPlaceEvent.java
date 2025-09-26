package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for when a block is placed
 * This event is fired when a block is placed, after being processed by our Bukkit listener
 */
public class MegaBlockPlaceEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final Location location;
    private final Material material;
    
    public MegaBlockPlaceEvent(Player player, Location location, Material material) {
        this.player = player;
        this.location = location;
        this.material = material;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}