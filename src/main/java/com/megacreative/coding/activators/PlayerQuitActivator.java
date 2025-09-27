package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles player quit events.
 * This activator listens to PlayerQuitEvent and triggers script execution.
 */
public class PlayerQuitActivator extends BukkitEventActivator {
    
    public PlayerQuitActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
    }
    
    @Override
    public String getEventName() {
        return "onPlayerQuit";
    }
    
    @Override
    public String getDisplayName() {
        return "Player Quit Event";
    }
    
    /**
     * Activates this activator for a player quit event
     * @param player The player who quit
     * @param quitMessage The quit message
     */
    public void activate(Player player, String quitMessage) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a game event with player quit context
        GameEvent gameEvent = new GameEvent("onPlayerQuit");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        }
        
        // Add custom data
        Map<String, Object> customData = new HashMap<>();
        customData.put("quitMessage", quitMessage);
        gameEvent.setCustomData(customData);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}