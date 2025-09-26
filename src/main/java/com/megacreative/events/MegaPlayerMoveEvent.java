package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for when a player moves
 * This event is fired when a player moves, after being processed by our Bukkit listener
 */
public class MegaPlayerMoveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final Location from;
    private final Location to;
    
    public MegaPlayerMoveEvent(Player player, Location from, Location to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Location getFrom() {
        return from;
    }
    
    public Location getTo() {
        return to;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}