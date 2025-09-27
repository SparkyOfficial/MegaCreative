package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;
import org.bukkit.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Factory class for creating GameEvents from Bukkit events with sophisticated data extraction.
 * This class provides a unified interface for converting Bukkit events to GameEvents
 * while leveraging the EventDataExtractor system for rich contextual data.
 */
public class GameEventFactory {
    
    private static final Logger LOGGER = Logger.getLogger(GameEventFactory.class.getName());
    
    private final EventDataExtractorRegistry extractorRegistry;
    
    public GameEventFactory() {
        this.extractorRegistry = new EventDataExtractorRegistry();
    }
    
    public GameEventFactory(EventDataExtractorRegistry extractorRegistry) {
        this.extractorRegistry = extractorRegistry;
    }
    
    /**
     * Creates a GameEvent from a Bukkit event with extracted data
     * @param eventName The name of the event
     * @param bukkitEvent The Bukkit event to extract data from
     * @param player The player associated with the event (can be null)
     * @return The created GameEvent with extracted data
     */
    public GameEvent createGameEvent(String eventName, Event bukkitEvent, Player player) {
        GameEvent gameEvent = new GameEvent(eventName);
        gameEvent.setPlayer(player);
        
        // Extract data using the EventDataExtractor system
        if (bukkitEvent != null && extractorRegistry.hasExtractor(bukkitEvent.getClass())) {
            try {
                Map<String, DataValue> extractedData = extractorRegistry.extractData(bukkitEvent);
                gameEvent.populateWithData(extractedData);
                
                LOGGER.fine("Successfully extracted " + extractedData.size() + " data points from " + bukkitEvent.getClass().getSimpleName());
            } catch (Exception e) {
                LOGGER.warning("Failed to extract data from " + bukkitEvent.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        
        return gameEvent;
    }
    
    /**
     * Creates a GameEvent with location data
     * @param eventName The name of the event
     * @param bukkitEvent The Bukkit event to extract data from
     * @param player The player associated with the event (can be null)
     * @param location The location associated with the event
     * @return The created GameEvent with extracted data and location
     */
    public GameEvent createGameEvent(String eventName, Event bukkitEvent, Player player, Location location) {
        GameEvent gameEvent = createGameEvent(eventName, bukkitEvent, player);
        gameEvent.setLocation(location);
        return gameEvent;
    }
    
    /**
     * Creates a GameEvent with custom data
     * @param eventName The name of the event
     * @param bukkitEvent The Bukkit event to extract data from
     * @param player The player associated with the event (can be null)
     * @param customData Custom data to add to the event
     * @return The created GameEvent with extracted data and custom data
     */
    public GameEvent createGameEvent(String eventName, Event bukkitEvent, Player player, Map<String, Object> customData) {
        GameEvent gameEvent = createGameEvent(eventName, bukkitEvent, player);
        
        if (customData != null) {
            for (Map.Entry<String, Object> entry : customData.entrySet()) {
                gameEvent.setCustomData(entry.getKey(), entry.getValue());
            }
        }
        
        return gameEvent;
    }
    
    /**
     * Gets the EventDataExtractorRegistry used by this factory
     * @return The EventDataExtractorRegistry
     */
    public EventDataExtractorRegistry getExtractorRegistry() {
        return extractorRegistry;
    }
}