package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

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
    
    public MegaPlayerTeleportEvent(@NotNull Player player, @NotNull Location from, @NotNull Location to, @NotNull String cause) {
        this.player = player;
        this.from = from;
        this.to = to;
        this.cause = cause;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public Location getFrom() {
        return from;
    }
    
    @NotNull
    public Location getTo() {
        return to;
    }
    
    @NotNull
    public String getCause() {
        return cause;
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