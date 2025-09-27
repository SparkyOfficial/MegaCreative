package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for when a player quits the server
 * This event is fired when a player quits, after being processed by our Bukkit listener
 */
public class MegaPlayerQuitEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String quitMessage;
    
    public MegaPlayerQuitEvent(Player player, String quitMessage) {
        this.player = player;
        this.quitMessage = quitMessage;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getQuitMessage() {
        return quitMessage;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}