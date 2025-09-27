package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.events.GameEventFactory;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles entity pickup item events.
 * This activator listens to EntityPickupItemEvent and triggers script execution.
 */
public class EntityPickupItemActivator extends BukkitEventActivator {
    
    private final GameEventFactory eventFactory;
    
    public EntityPickupItemActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
        this.eventFactory = new GameEventFactory();
    }
    
    @Override
    public String getEventName() {
        return "onEntityPickupItem";
    }
    
    @Override
    public String getDisplayName() {
        return "Entity Pickup Item Event";
    }
    
    /**
     * Activates this activator for an entity pickup item event
     * @param player The player who picked up the item
     * @param item The item that was picked up
     * @param quantity The quantity of items picked up
     */
    public void activate(Player player, ItemStack item, int quantity) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a Bukkit event to extract data from
        EntityPickupItemEvent bukkitEvent = new EntityPickupItemEvent(player, item, quantity);
        
        // Create custom data for backward compatibility
        Map<String, Object> customData = new HashMap<>();
        customData.put("item", item);
        customData.put("quantity", quantity);
        
        // Create a game event with entity pickup item context using the factory
        GameEvent gameEvent = eventFactory.createGameEvent("onEntityPickupItem", bukkitEvent, player, customData);
        gameEvent.setLocation(player.getLocation());
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}