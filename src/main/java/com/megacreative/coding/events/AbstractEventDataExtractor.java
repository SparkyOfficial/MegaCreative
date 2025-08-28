package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base implementation of EventDataExtractor providing common functionality
 */
public abstract class AbstractEventDataExtractor<T extends Event> implements EventDataExtractor<T> {
    
    private final Class<T> eventType;
    private final Map<String, String> variableDescriptions = new HashMap<>();
    private final Set<String> providedVariables = new LinkedHashSet<>();
    
    protected AbstractEventDataExtractor(Class<T> eventType) {
        this.eventType = eventType;
        initializeVariables();
    }
    
    @Override
    public Class<T> getEventType() {
        return eventType;
    }
    
    @Override
    public Set<String> getProvidedVariables() {
        return new LinkedHashSet<>(providedVariables);
    }
    
    @Override
    public Map<String, String> getVariableDescriptions() {
        return new HashMap<>(variableDescriptions);
    }
    
    /**
     * Initialize variable descriptions - called in constructor
     * Subclasses should override this to define their variables
     */
    protected abstract void initializeVariables();
    
    /**
     * Helper method to register a variable with description
     */
    protected void registerVariable(String name, String description) {
        providedVariables.add(name);
        variableDescriptions.put(name, description);
    }
    
    /**
     * Helper method to extract common player data
     */
    protected void extractPlayerData(Map<String, DataValue> data, Player player) {
        if (player != null) {
            data.put("playerName", DataValue.fromObject(player.getName()));
            data.put("playerUUID", DataValue.fromObject(player.getUniqueId().toString()));
            data.put("playerDisplayName", DataValue.fromObject(player.getDisplayName()));
            data.put("playerHealth", DataValue.fromObject(player.getHealth()));
            data.put("playerFoodLevel", DataValue.fromObject(player.getFoodLevel()));
            data.put("playerGameMode", DataValue.fromObject(player.getGameMode().name()));
            data.put("playerLevel", DataValue.fromObject(player.getLevel()));
            data.put("playerExp", DataValue.fromObject(player.getExp()));
        }
    }
    
    /**
     * Helper method to extract common location data
     */
    protected void extractLocationData(Map<String, DataValue> data, Location location, String prefix) {
        if (location != null) {
            data.put(prefix + "X", DataValue.fromObject(location.getBlockX()));
            data.put(prefix + "Y", DataValue.fromObject(location.getBlockY()));
            data.put(prefix + "Z", DataValue.fromObject(location.getBlockZ()));
            data.put(prefix + "World", DataValue.fromObject(location.getWorld().getName()));
            data.put(prefix + "Location", DataValue.fromObject(
                location.getWorld().getName() + "," + 
                location.getBlockX() + "," + 
                location.getBlockY() + "," + 
                location.getBlockZ()));
            data.put(prefix + "Yaw", DataValue.fromObject(location.getYaw()));
            data.put(prefix + "Pitch", DataValue.fromObject(location.getPitch()));
        }
    }
}