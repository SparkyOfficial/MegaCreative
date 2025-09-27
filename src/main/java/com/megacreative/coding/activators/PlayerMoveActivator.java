package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles player move events.
 * This activator listens to PlayerMoveEvent and triggers script execution.
 */
public class PlayerMoveActivator extends BukkitEventActivator {
    
    private boolean onlyOnNewBlock = true;
    
    public PlayerMoveActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
    }
    
    /**
     * Sets whether this activator should only trigger when moving to a new block
     * @param onlyOnNewBlock true to only trigger on new block, false to trigger on any movement
     */
    public void setOnlyOnNewBlock(boolean onlyOnNewBlock) {
        this.onlyOnNewBlock = onlyOnNewBlock;
    }
    
    /**
     * Checks if this activator only triggers when moving to a new block
     * @return true if only triggering on new block, false otherwise
     */
    public boolean isOnlyOnNewBlock() {
        return onlyOnNewBlock;
    }
    
    @Override
    public String getEventName() {
        return "onPlayerMove";
    }
    
    @Override
    public String getDisplayName() {
        return "Player Move Event";
    }
    
    /**
     * Activates this activator for a player move event
     * @param player The player who moved
     * @param from The location the player moved from
     * @param to The location the player moved to
     */
    public void activate(Player player, Location from, Location to) {
        if (!enabled || script == null) {
            return;
        }
        
        // Check if we should only trigger on new block movement
        if (onlyOnNewBlock) {
            if (from.getBlockX() == to.getBlockX() && 
                from.getBlockY() == to.getBlockY() && 
                from.getBlockZ() == to.getBlockZ()) {
                return;
            }
        }
        
        // Create a game event with player move context
        GameEvent gameEvent = new GameEvent("onPlayerMove");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        }
        
        // Add custom data
        Map<String, Object> customData = new HashMap<>();
        customData.put("from", from);
        customData.put("to", to);
        gameEvent.setCustomData(customData);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}