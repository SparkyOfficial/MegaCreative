package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Event triggered when a function is called
 */
public class FunctionCallEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String functionName;
    private final Map<String, Object> parameters;
    
    public FunctionCallEvent(@NotNull Player player, @NotNull String functionName, @NotNull Map<String, Object> parameters) {
        this.player = player;
        this.functionName = functionName;
        this.parameters = parameters;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public String getFunctionName() {
        return functionName;
    }
    
    @NotNull
    public Map<String, Object> getParameters() {
        return parameters;
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