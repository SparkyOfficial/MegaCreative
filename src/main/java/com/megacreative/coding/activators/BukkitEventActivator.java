package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.entity.Player;

/**
 * Base class for activators that handle Bukkit events.
 * This class provides common functionality for converting Bukkit events to GameEvents.
 */
public abstract class BukkitEventActivator extends Activator {
    
    protected Location location;
    
    public BukkitEventActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
    }
    
    /**
     * Sets the location of this activator in the world
     * @param location The location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }
    
    @Override
    public Location getLocation() {
        return location;
    }
    
    /**
     * Creates a GameEvent from a Bukkit event with common properties
     * @param eventName The name of the event
     * @param bukkitEvent The Bukkit event
     * @param player The player associated with the event (can be null)
     * @return The created GameEvent
     */
    protected GameEvent createGameEvent(String eventName, Event bukkitEvent, Player player) {
        GameEvent gameEvent = new GameEvent(eventName);
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        }
        return gameEvent;
    }
}