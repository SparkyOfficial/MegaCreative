package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Custom event for when a player chats
 * This event is fired when a player sends a chat message, after being processed by our Bukkit listener
 */
public class MegaPlayerChatEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String message;
    
    public MegaPlayerChatEvent(@NotNull Player player, @NotNull String message) {
        this.player = player;
        this.message = message;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public String getMessage() {
        return message;
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