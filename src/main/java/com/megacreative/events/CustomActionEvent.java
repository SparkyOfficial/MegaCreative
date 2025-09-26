package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;

/**
 * Event triggered when a player performs a custom action
 */
public class CustomActionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String actionName;
    private final Map<String, Object> actionData;
    
    public CustomActionEvent(Player player, String actionName, Map<String, Object> actionData) {
        this.player = player;
        this.actionName = actionName;
        this.actionData = actionData;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getActionName() {
        return actionName;
    }
    
    public Map<String, Object> getActionData() {
        return actionData;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}