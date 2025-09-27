package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles entity damage events.
 * This activator listens to EntityDamageEvent and triggers script execution.
 */
public class EntityDamageActivator extends BukkitEventActivator {
    
    public EntityDamageActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
    }
    
    @Override
    public String getEventName() {
        return "onEntityDamage";
    }
    
    @Override
    public String getDisplayName() {
        return "Entity Damage Event";
    }
    
    /**
     * Activates this activator for an entity damage event
     * @param entity The entity that was damaged
     * @param damage The amount of damage
     * @param cause The cause of the damage
     */
    public void activate(Entity entity, double damage, String cause) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a game event with entity damage context
        GameEvent gameEvent = new GameEvent("onEntityDamage");
        
        // Set player if entity is a player
        if (entity instanceof Player) {
            gameEvent.setPlayer((Player) entity);
        }
        
        if (location != null) {
            gameEvent.setLocation(location);
        } else if (entity.getLocation() != null) {
            gameEvent.setLocation(entity.getLocation());
        }
        
        // Add custom data
        Map<String, Object> customData = new HashMap<>();
        customData.put("damage", damage);
        customData.put("cause", cause);
        
        // Add entity information
        customData.put("entityType", entity.getType().name());
        if (entity instanceof Player) {
            customData.put("playerName", ((Player) entity).getName());
        }
        
        gameEvent.setCustomData(customData);
        
        // Activate the script
        super.activate(gameEvent, entity instanceof Player ? (Player) entity : null);
    }
}