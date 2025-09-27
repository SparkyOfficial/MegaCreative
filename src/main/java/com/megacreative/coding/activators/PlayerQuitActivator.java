package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.events.GameEventFactory;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles player quit events.
 * This activator listens to PlayerQuitEvent and triggers script execution.
 */
public class PlayerQuitActivator extends BukkitEventActivator {
    
    private final GameEventFactory eventFactory;
    
    public PlayerQuitActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
        this.eventFactory = new GameEventFactory();
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
        
        // Create a Bukkit event to extract data from
        PlayerQuitEvent bukkitEvent = new PlayerQuitEvent(player, quitMessage);
        
        // Create custom data for backward compatibility
        Map<String, Object> customData = new HashMap<>();
        customData.put("quitMessage", quitMessage);
        
        // Create a game event with player quit context using the factory
        GameEvent gameEvent = eventFactory.createGameEvent("onPlayerQuit", bukkitEvent, player, customData);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}