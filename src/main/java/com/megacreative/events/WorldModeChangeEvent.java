package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a world's mode changes
 */
public class WorldModeChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final String worldName;
    private final String oldMode;
    private final String newMode;
    
    public WorldModeChangeEvent(@NotNull Player player, @NotNull String worldName, @NotNull String oldMode, @NotNull String newMode) {
        this.player = player;
        this.worldName = worldName;
        this.oldMode = oldMode;
        this.newMode = newMode;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public String getWorldName() {
        return worldName;
    }
    
    @NotNull
    public String getOldMode() {
        return oldMode;
    }
    
    @NotNull
    public String getNewMode() {
        return newMode;
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