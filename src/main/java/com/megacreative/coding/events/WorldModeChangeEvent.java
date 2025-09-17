package com.megacreative.coding.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a world's mode changes
 */
public class WorldModeChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String worldName;
    private final String oldMode;
    private final String newMode;
    
    public WorldModeChangeEvent(Player player, String worldName, String oldMode, String newMode) {
        this.player = player;
        this.worldName = worldName;
        this.oldMode = oldMode;
        this.newMode = newMode;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public String getOldMode() {
        return oldMode;
    }
    
    public String getNewMode() {
        return newMode;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}