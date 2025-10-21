package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a custom timer expires
 */
public class CustomTimerExpireEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String timerName;
    private final long duration;
    
    public CustomTimerExpireEvent(@NotNull Player player, @NotNull String timerName, long duration) {
        this.player = player;
        this.timerName = timerName;
        this.duration = duration;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public String getTimerName() {
        return timerName;
    }
    
    public long getDuration() {
        return duration;
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