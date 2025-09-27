package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles player respawn events.
 * This activator listens to PlayerRespawnEvent and triggers script execution.
 */
public class PlayerRespawnActivator extends BukkitEventActivator {
    
    public PlayerRespawnActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
    }
    
    @Override
    public String getEventName() {
        return "onPlayerRespawn";
    }
    
    @Override
    public String getDisplayName() {
        return "Player Respawn Event";
    }
    
    /**
     * Activates this activator for a player respawn event
     * @param player The player who respawned
     * @param respawnLocation The location where the player respawned
     * @param isBedSpawn Whether the respawn was at a bed
     */
    public void activate(Player player, Location respawnLocation, boolean isBedSpawn) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a game event with player respawn context
        GameEvent gameEvent = new GameEvent("onPlayerRespawn");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        } else if (respawnLocation != null) {
            gameEvent.setLocation(respawnLocation);
        }
        
        // Add custom data
        Map<String, Object> customData = new HashMap<>();
        customData.put("respawnLocation", respawnLocation);
        customData.put("isBedSpawn", isBedSpawn);
        gameEvent.setCustomData(customData);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}