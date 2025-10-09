package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a game event with context data.
 * This class carries all the contextual information needed for script execution,
 * including player data, location data, and event-specific information.
 */
public class GameEvent {
    
    private final String eventName;
    private final long timestamp;
    private Player player;
    private Entity entity;
    private Location location;
    private boolean firstJoin = false;
    private String message;
    private ItemStack item;
    private Map<String, Object> customData;
    
    public GameEvent(String eventName) {
        this.eventName = eventName;
        this.timestamp = System.currentTimeMillis();
        this.customData = new HashMap<>();
    }
    
    /**
     * Gets the name of the event
     */
    public String getEventName() {
        return eventName;
    }
    
    /**
     * Gets the timestamp when the event occurred
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the player associated with this event
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Sets the player associated with this event
     */
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    /**
     * Gets the entity associated with this event
     */
    public Entity getEntity() {
        return entity;
    }
    
    /**
     * Sets the entity associated with this event
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
    }
    
    /**
     * Gets the location where the event occurred
     */
    public Location getLocation() {
        return location;
    }
    
    /**
     * Sets the location where the event occurred
     */
    public void setLocation(Location location) {
        this.location = location;
    }
    
    /**
     * Checks if this is the player's first join
     */
    public boolean isFirstJoin() {
        return firstJoin;
    }
    
    /**
     * Sets whether this is the player's first join
     */
    public void setFirstJoin(boolean firstJoin) {
        this.firstJoin = firstJoin;
    }
    
    /**
     * Gets the message associated with this event (for chat events)
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the message associated with this event (for chat events)
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Gets the item associated with this event (for item events)
     */
    public ItemStack getItem() {
        return item;
    }
    
    /**
     * Sets the item associated with this event (for item events)
     */
    public void setItem(ItemStack item) {
        this.item = item;
    }
    
    /**
     * Gets custom data associated with this event
     */
    public Map<String, Object> getCustomData() {
        return customData;
    }
    
    /**
     * Sets custom data associated with this event
     */
    public void setCustomData(Map<String, Object> customData) {
        this.customData = customData;
    }
    
    /**
     * Gets a custom data value by key
     */
    public Object getCustomData(String key) {
        return customData.get(key);
    }
    
    /**
     * Sets a custom data value by key
     */
    public void setCustomData(String key, Object value) {
        customData.put(key, value);
    }
    
    /**
     * Removes a custom data value by key
     */
    public void removeCustomData(String key) {
        customData.remove(key);
    }
    
    /**
     * Populates this GameEvent with data extracted from a Bukkit event
     * @param extractedData Map of variable names to DataValue objects
     */
    public void populateWithData(Map<String, DataValue> extractedData) {
        if (extractedData != null) {
            for (Map.Entry<String, DataValue> entry : extractedData.entrySet()) {
                customData.put(entry.getKey(), entry.getValue().getValue());
            }
        }
    }
    
    /**
     * Gets event data as a Map of String to DataValue
     * @return Map of event data
     */
    public Map<String, DataValue> getEventData() {
        Map<String, DataValue> eventData = new HashMap<>();
        for (Map.Entry<String, Object> entry : customData.entrySet()) {
            eventData.put(entry.getKey(), DataValue.fromObject(entry.getValue()));
        }
        return eventData;
    }
}