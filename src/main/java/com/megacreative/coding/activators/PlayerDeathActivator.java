package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.events.GameEventFactory;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles player death events.
 * This activator listens to PlayerDeathEvent and triggers script execution.
 */
public class PlayerDeathActivator extends BukkitEventActivator {
    
    private final GameEventFactory eventFactory;
    
    public PlayerDeathActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
        this.eventFactory = new GameEventFactory();
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
        
        // Create a Bukkit event to extract data from
        PlayerDeathEvent bukkitEvent = new PlayerDeathEvent(player, null, 0, deathMessage);
        
        // Create custom data for backward compatibility
        Map<String, Object> customData = new HashMap<>();
        customData.put("deathMessage", deathMessage);
        
        // Create a game event with player death context using the factory
        GameEvent gameEvent = eventFactory.createGameEvent("onPlayerDeath", bukkitEvent, player, customData);
        gameEvent.setLocation(player.getLocation());
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}