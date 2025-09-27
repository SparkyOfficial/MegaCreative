package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for when a player respawns
 * This event is fired when a player respawns, after being processed by our Bukkit listener
 */
public class MegaPlayerRespawnEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final Location respawnLocation;
    private final boolean isBedSpawn;
    
    public MegaPlayerRespawnEvent(Player player, Location respawnLocation, boolean isBedSpawn) {
        this.player = player;
        this.respawnLocation = respawnLocation;
        this.isBedSpawn = isBedSpawn;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Location getRespawnLocation() {
        return respawnLocation;
    }
    
    public boolean isBedSpawn() {
        return isBedSpawn;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}