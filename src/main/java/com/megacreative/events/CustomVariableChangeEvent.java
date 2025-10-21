package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a player's variable changes
 */
public class CustomVariableChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String variableName;
    private final Object oldValue;
    private final Object newValue;
    
    public CustomVariableChangeEvent(@NotNull Player player, @NotNull String variableName, Object oldValue, Object newValue) {
        this.player = player;
        this.variableName = variableName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
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
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
    
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}