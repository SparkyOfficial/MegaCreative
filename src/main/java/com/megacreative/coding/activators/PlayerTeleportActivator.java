package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.events.GameEventFactory;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles player teleport events.
 * This activator listens to PlayerTeleportEvent and triggers script execution.
 */
public class PlayerTeleportActivator extends BukkitEventActivator {
    
    private final GameEventFactory eventFactory;
    
    public PlayerTeleportActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
        this.eventFactory = new GameEventFactory();
    }
    
    @Override
    public String getEventName() {
        return "onPlayerTeleport";
    }
    
    @Override
    public String getDisplayName() {
        return "Player Teleport Event";
    }
    
    /**
     * Activates this activator for a player teleport event
     * @param player The player who teleported
     * @param from The location the player teleported from
     * @param to The location the player teleported to
     * @param cause The cause of the teleportation
     */
    public void activate(Player player, Location from, Location to, String cause) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a Bukkit event to extract data from
        PlayerTeleportEvent bukkitEvent = new PlayerTeleportEvent(player, from, to, PlayerTeleportEvent.TeleportCause.valueOf(cause));
        
        // Create custom data for backward compatibility
        Map<String, Object> customData = new HashMap<>();
        customData.put("from", from);
        customData.put("to", to);
        customData.put("cause", cause);
        
        // Create a game event with player teleport context using the factory
        GameEvent gameEvent = eventFactory.createGameEvent("onPlayerTeleport", bukkitEvent, player, customData);
        gameEvent.setLocation(to);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}