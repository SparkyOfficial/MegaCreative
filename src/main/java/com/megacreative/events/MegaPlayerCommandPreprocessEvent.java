package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for when a player preprocesses a command
 * This event is fired when a player uses a command, after being processed by our Bukkit listener
 */
public class MegaPlayerCommandPreprocessEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String command;
    
    public MegaPlayerCommandPreprocessEvent(Player player, String command) {
        this.player = player;
        this.command = command;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getCommand() {
        return command;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}