package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for when a player dies
 * This event is fired when a player dies, after being processed by our Bukkit listener
 */
public class MegaPlayerDeathEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String deathMessage;
    
    public MegaPlayerDeathEvent(Player player, String deathMessage) {
        this.player = player;
        this.deathMessage = deathMessage;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getDeathMessage() {
        return deathMessage;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}