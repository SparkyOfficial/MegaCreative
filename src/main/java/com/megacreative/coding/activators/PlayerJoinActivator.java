package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.events.GameEventFactory;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles player join events.
 * This activator listens to PlayerJoinEvent and triggers script execution.
 */
public class PlayerJoinActivator extends Activator {
    
    private Location location;
    private boolean isFirstJoin = false;
    private final GameEventFactory eventFactory;
    
    public PlayerJoinActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
        this.eventFactory = new GameEventFactory();
    }
    
    /**
     * Sets whether this is the first join for the player
     */
    public void setFirstJoin(boolean firstJoin) {
        this.isFirstJoin = firstJoin;
    }
    
    /**
     * Checks if this is the first join for the player
     */
    public boolean isFirstJoin() {
        return isFirstJoin;
    }
    
    /**
     * Sets the location of this activator in the world
     */
    public void setLocation(Location location) {
        this.location = location;
    }
    
    @Override
    public String getEventName() {
        return "onJoin";
    }
    
    @Override
    public String getDisplayName() {
        return "Player Join Event";
    }
    
    @Override
    public Location getLocation() {
        return location;
    }
    
    /**
     * Activates this activator for a player join event
     * @param player The player who joined
     * @param isFirstJoin Whether this is the player's first join
     */
    public void activate(Player player, boolean isFirstJoin) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a Bukkit event to extract data from
        PlayerJoinEvent bukkitEvent = new PlayerJoinEvent(player, "joined the game");
        
        // Create a game event with player join context using the factory
        GameEvent gameEvent = eventFactory.createGameEvent("onJoin", bukkitEvent, player);
        gameEvent.setFirstJoin(isFirstJoin);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}