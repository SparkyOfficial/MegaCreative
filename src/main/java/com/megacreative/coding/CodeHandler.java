package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.activators.Activator;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Centralized script execution manager that handles the flow from activators to actions.
 * This class serves as the "glue" that connects the event system to the script execution system.
 * 
 * CodeHandler is responsible for:
 * 1. Managing all activators in a creative world
 * 2. Receiving events from activators
 * 3. Finding and executing the appropriate scripts
 * 4. Coordinating the execution flow
 */
public class CodeHandler {
    
    private static final Logger LOGGER = Logger.getLogger(CodeHandler.class.getName());
    
    private final MegaCreative plugin;
    private final CreativeWorld creativeWorld;
    private final Map<String, Activator> activators;
    private final ScriptEngine scriptEngine;
    
    public CodeHandler(MegaCreative plugin, CreativeWorld creativeWorld) {
        this.plugin = plugin;
        this.creativeWorld = creativeWorld;
        this.activators = new ConcurrentHashMap<>();
        this.scriptEngine = plugin.getServiceRegistry().getScriptEngine();
    }
    
    /**
     * Registers an activator with this code handler
     * @param activator The activator to register
     */
    public void registerActivator(Activator activator) {
        activators.put(activator.getId(), activator);
        LOGGER.info("Registered activator: " + activator.getId() + " for event: " + activator.getEventName());
    }
    
    /**
     * Unregisters an activator from this code handler
     * @param activatorId The ID of the activator to unregister
     */
    public void unregisterActivator(String activatorId) {
        Activator removed = activators.remove(activatorId);
        if (removed != null) {
            LOGGER.info("Unregistered activator: " + activatorId);
        }
    }
    
    /**
     * Gets an activator by its ID
     * @param activatorId The ID of the activator to get
     * @return The activator, or null if not found
     */
    public Activator getActivator(String activatorId) {
        return activators.get(activatorId);
    }
    
    /**
     * Gets all activators for a specific event name
     * @param eventName The event name to filter by
     * @return List of activators for the event
     */
    public List<Activator> getActivatorsForEvent(String eventName) {
        List<Activator> result = new ArrayList<>();
        for (Activator activator : activators.values()) {
            if (activator.getEventName().equals(eventName)) {
                result.add(activator);
            }
        }
        return result;
    }
    
    /**
     * Handles an event by executing all scripts associated with activators for that event
     * @param eventName The name of the event that occurred
     * @param gameEvent The game event containing context data
     * @param player The player associated with the event (can be null)
     */
    public void handleEvent(String eventName, GameEvent gameEvent, Player player) {
        // Find all activators for this event
        List<Activator> eventActivators = getActivatorsForEvent(eventName);
        
        if (eventActivators.isEmpty()) {
            LOGGER.fine("No activators found for event: " + eventName);
            return;
        }
        
        LOGGER.info("Handling event: " + eventName + " with " + eventActivators.size() + " activators");
        
        // Execute scripts for each activator
        for (Activator activator : eventActivators) {
            if (activator.isEnabled() && activator.getScript() != null) {
                try {
                    // Execute the script through the script engine
                    scriptEngine.executeScript(activator.getScript(), player, eventName);
                } catch (Exception e) {
                    LOGGER.severe("Error executing script for activator " + activator.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Gets all registered activators
     * @return Collection of all activators
     */
    public Collection<Activator> getAllActivators() {
        return new ArrayList<>(activators.values());
    }
    
    /**
     * Gets the creative world this code handler manages
     * @return The creative world
     */
    public CreativeWorld getCreativeWorld() {
        return creativeWorld;
    }
    
    /**
     * Gets the script engine used by this code handler
     * @return The script engine
     */
    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }
    
    /**
     * Clears all activators from this code handler
     */
    public void clearActivators() {
        activators.clear();
        LOGGER.info("Cleared all activators");
    }
    
    /**
     * Gets the number of registered activators
     * @return The count of activators
     */
    public int getActivatorCount() {
        return activators.size();
    }
}