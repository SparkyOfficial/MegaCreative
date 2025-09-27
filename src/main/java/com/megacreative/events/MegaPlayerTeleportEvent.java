package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for when a player teleports
 * This event is fired when a player teleports, after being processed by our Bukkit listener
 */
public class MegaPlayerTeleportEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final Location from;
    private final Location to;
    private final String cause;
    
    public MegaPlayerTeleportEvent(Player player, Location from, Location to, String cause) {
        this.player = player;
        this.from = from;
        this.to = to;
        this.cause = cause;
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
    
    public String getCause() {
        return cause;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}