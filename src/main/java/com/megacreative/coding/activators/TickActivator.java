package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.events.GameEventFactory;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;

/**
 * Activator that handles tick events.
 * This activator listens to tick events and triggers script execution.
 */
public class TickActivator extends Activator {
    
    private Location location;
    private final GameEventFactory eventFactory;
    private String tickType; // "onTick", "onSecond", "onMinute"
    
    public TickActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine, String tickType) {
        super(creativeWorld, scriptEngine);
        this.eventFactory = new GameEventFactory();
        this.tickType = tickType;
    }
    
    /**
     * Sets the tick type for this activator
     */
    public void setTickType(String tickType) {
        this.tickType = tickType;
    }
    
    /**
     * Gets the tick type for this activator
     */
    public String getTickType() {
        return tickType;
    }
    
    /**
     * Sets the location of this activator in the world
     */
    public void setLocation(Location location) {
        this.location = location;
    }
    
    @Override
    public String getEventName() {
        return tickType;
    }
    
    @Override
    public String getDisplayName() {
        switch (tickType) {
            case "onSecond":
                return "Second Tick Event";
            case "onMinute":
                return "Minute Tick Event";
            default:
                return "Tick Event";
        }
    }
    
    @Override
    public Location getLocation() {
        return location;
    }
    
    /**
     * Activates this activator for a tick event
     * @param tickNumber The current tick number
     */
    public void activate(long tickNumber) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a game event with tick context
        GameEvent gameEvent = new GameEvent(tickType);
        gameEvent.setCustomData("tick", tickNumber);
        
        // Activate the script
        super.activate(gameEvent, null);
    }
}