package com.megacreative.coding.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Базовый класс для пользовательских событий.
 */
public abstract class CustomEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
