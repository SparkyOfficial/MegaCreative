package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a player's score changes
 */
public class PlayerScoreChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String scoreType;
    private final double oldScore;
    private final double newScore;
    
    public PlayerScoreChangeEvent(@NotNull Player player, @NotNull String scoreType, double oldScore, double newScore) {
        this.player = player;
        this.scoreType = scoreType;
        this.oldScore = oldScore;
        this.newScore = newScore;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public String getScoreType() {
        return scoreType;
    }
    
    public double getOldScore() {
        return oldScore;
    }
    
    public double getNewScore() {
        return newScore;
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