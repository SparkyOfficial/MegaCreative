package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a player's variable changes
 */
public class CustomVariableChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String variableName;
    private final Object oldValue;
    private final Object newValue;
    
    public CustomVariableChangeEvent(Player player, String variableName, Object oldValue, Object newValue) {
        this.player = player;
        this.variableName = variableName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getVariableName() {
        return variableName;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}