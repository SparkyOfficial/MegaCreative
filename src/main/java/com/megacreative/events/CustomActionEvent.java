package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Event triggered when a player performs a custom action
 */
public class CustomActionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String actionName;
    private final Map<String, Object> actionData;
    
    public CustomActionEvent(@NotNull Player player, @NotNull String actionName, @NotNull Map<String, Object> actionData) {
        this.player = player;
        this.actionName = actionName;
        this.actionData = actionData;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public String getActionName() {
        return actionName;
    }
    
    @NotNull
    public Map<String, Object> getActionData() {
        return actionData;
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