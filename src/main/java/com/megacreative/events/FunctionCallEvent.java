package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;

/**
 * Event triggered when a function is called
 */
public class FunctionCallEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String functionName;
    private final Map<String, Object> parameters;
    
    public FunctionCallEvent(Player player, String functionName, Map<String, Object> parameters) {
        this.player = player;
        this.functionName = functionName;
        this.parameters = parameters;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getFunctionName() {
        return functionName;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}