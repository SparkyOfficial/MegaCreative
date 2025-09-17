package com.megacreative.coding.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a custom timer expires
 */
public class CustomTimerExpireEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String timerName;
    private final long duration;
    
    public CustomTimerExpireEvent(Player player, String timerName, long duration) {
        this.player = player;
        this.timerName = timerName;
        this.duration = duration;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getTimerName() {
        return timerName;
    }
    
    public long getDuration() {
        return duration;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}