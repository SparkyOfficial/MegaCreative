package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for when a player chats
 * This event is fired when a player sends a chat message, after being processed by our Bukkit listener
 */
public class MegaPlayerChatEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String message;
    
    public MegaPlayerChatEvent(Player player, String message) {
        this.player = player;
        this.message = message;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}