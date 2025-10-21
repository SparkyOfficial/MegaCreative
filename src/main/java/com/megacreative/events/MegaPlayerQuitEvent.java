package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Custom event for when a player quits the server
 * This event is fired when a player quits, after being processed by our Bukkit listener
 */
public class MegaPlayerQuitEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String quitMessage;
    
    public MegaPlayerQuitEvent(@NotNull Player player, @NotNull String quitMessage) {
        this.player = player;
        this.quitMessage = quitMessage;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public String getQuitMessage() {
        return quitMessage;
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