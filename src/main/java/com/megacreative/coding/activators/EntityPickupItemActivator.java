package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles entity pickup item events.
 * This activator listens to EntityPickupItemEvent and triggers script execution.
 */
public class EntityPickupItemActivator extends Activator {
    
    private Location location;
    
    public EntityPickupItemActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
    }
    
    /**
     * Sets the location of this activator in the world
     * @param location The location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }
    
    @Override
    public Location getLocation() {
        return location;
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
        
        // Create a game event with entity pickup item context
        GameEvent gameEvent = new GameEvent("onEntityPickupItem");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        }
        
        // Add custom data
        Map<String, Object> customData = new HashMap<>();
        if (item != null) {
            customData.put("itemType", item.getType().name());
            customData.put("itemName", item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
                ? item.getItemMeta().getDisplayName() : "");
        }
        customData.put("quantity", quantity);
        gameEvent.setCustomData(customData);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}