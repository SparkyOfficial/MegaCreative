package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles player teleport events.
 * This activator listens to PlayerTeleportEvent and triggers script execution.
 */
public class PlayerTeleportActivator extends BukkitEventActivator {
    
    public PlayerTeleportActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
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
        
        // Create a game event with player teleport context
        GameEvent gameEvent = new GameEvent("onPlayerTeleport");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        } else if (to != null) {
            gameEvent.setLocation(to);
        }
        
        // Add custom data
        Map<String, Object> customData = new HashMap<>();
        customData.put("from", from);
        customData.put("to", to);
        customData.put("cause", cause);
        gameEvent.setCustomData(customData);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}