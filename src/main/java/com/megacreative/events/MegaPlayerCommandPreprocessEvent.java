package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Custom event for when a player preprocesses a command
 * This event is fired when a player uses a command, after being processed by our Bukkit listener
 */
public class MegaPlayerCommandPreprocessEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String command;
    
    public MegaPlayerCommandPreprocessEvent(@NotNull Player player, @NotNull String command) {
        this.player = player;
        this.command = command;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public String getCommand() {
        return command;
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