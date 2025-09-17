package com.megacreative.coding.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a player's score changes
 */
public class PlayerScoreChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String scoreType;
    private final double oldScore;
    private final double newScore;
    
    public PlayerScoreChangeEvent(Player player, String scoreType, double oldScore, double newScore) {
        this.player = player;
        this.scoreType = scoreType;
        this.oldScore = oldScore;
        this.newScore = newScore;
    }
    
    public Player getPlayer() {
        return player;
    }
    
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
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}