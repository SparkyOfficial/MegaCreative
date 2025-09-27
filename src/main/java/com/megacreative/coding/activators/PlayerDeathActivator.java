package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles player death events.
 * This activator listens to PlayerDeathEvent and triggers script execution.
 */
public class PlayerDeathActivator extends BukkitEventActivator {
    
    public PlayerDeathActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
    }
    
    @Override
    public String getEventName() {
        return "onPlayerDeath";
    }
    
    @Override
    public String getDisplayName() {
        return "Player Death Event";
    }
    
    /**
     * Activates this activator for a player death event
     * @param player The player who died
     * @param deathMessage The death message
     */
    public void activate(Player player, String deathMessage) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a game event with player death context
        GameEvent gameEvent = new GameEvent("onPlayerDeath");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        } else if (player.getLocation() != null) {
            gameEvent.setLocation(player.getLocation());
        }
        
        // Add custom data
        Map<String, Object> customData = new HashMap<>();
        customData.put("deathMessage", deathMessage);
        gameEvent.setCustomData(customData);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}