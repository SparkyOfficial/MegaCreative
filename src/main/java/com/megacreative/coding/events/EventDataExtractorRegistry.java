package com.megacreative.coding.events;

import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.events.extractors.*;
import com.megacreative.coding.values.DataValue;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central registry for event data extractors
 * Manages registration and lookup of extractors for different event types
 */
public class EventDataExtractorRegistry {
    
    private static final Logger log = Logger.getLogger(EventDataExtractorRegistry.class.getName());
    
    private final Map<Class<? extends Event>, EventDataExtractor<? extends Event>> extractors = new ConcurrentHashMap<>();
    
    /**
     * Constructor - registers all default extractors
     */
    public EventDataExtractorRegistry() {
        registerDefaultExtractors();
    }
    
    /**
     * Registers default extractors for common Bukkit events
     */
    private void registerDefaultExtractors() {
        registerExtractor(new PlayerJoinEventDataExtractor());
        registerExtractor(new PlayerDeathEventDataExtractor());
        registerExtractor(new BlockBreakEventDataExtractor());
        registerExtractor(new PlayerInteractEventDataExtractor());
        registerExtractor(new PlayerCommandPreprocessEventDataExtractor());
        
        log.info("Registered " + extractors.size() + " event data extractors");
    }
    
    /**
     * Registers a new event data extractor
     * @param extractor The extractor to register
     */
    public <T extends Event> void registerExtractor(EventDataExtractor<T> extractor) {
        extractors.put(extractor.getEventType(), extractor);
        log.info("Registered extractor for " + extractor.getEventType().getSimpleName());
    }
    
    /**
     * Gets an extractor for a specific event type
     * @param eventType The event type class
     * @return The extractor, or null if none found
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> EventDataExtractor<T> getExtractor(Class<T> eventType) {
        return (EventDataExtractor<T>) extractors.get(eventType);
    }
    
    /**
     * Gets an extractor for a specific event instance
     * @param event The event instance
     * @return The extractor, or null if none found
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> EventDataExtractor<T> getExtractor(T event) {
        return (EventDataExtractor<T>) extractors.get(event.getClass());
    }
    
    /**
     * Checks if an extractor exists for the given event type
     * @param eventType The event type to check
     * @return true if an extractor exists
     */
    public boolean hasExtractor(Class<? extends Event> eventType) {
        return extractors.containsKey(eventType);
    }
    
    /**
     * Extracts data from an event using the appropriate extractor
     * @param event The event to extract data from
     * @return Map of variable names to DataValue objects, or empty map if no extractor found
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataValue> extractData(Event event) {
        EventDataExtractor<Event> extractor = (EventDataExtractor<Event>) getExtractor(event);
        if (extractor != null) {
            try {
                return extractor.extractData(event);
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to extract data from " + event.getClass().getSimpleName(), e);
            }
        }
        return new HashMap<>();
    }
    
    /**
     * Populates an execution context with data from an event
     * @param event The event to extract data from
     * @param context The execution context to populate
     * @return true if data was extracted and applied successfully
     */
    @SuppressWarnings("unchecked")
    public boolean populateContext(Event event, ExecutionContext context) {
        EventDataExtractor<Event> extractor = (EventDataExtractor<Event>) getExtractor(event);
        if (extractor != null) {
            try {
                extractor.populateContext(event, context);
                return true;
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to populate context from " + event.getClass().getSimpleName(), e);
            }
        }
        return false;
    }
    
    /**
     * Gets all variable names provided by an extractor for a specific event type
     * @param eventType The event type
     * @return Set of variable names, or empty set if no extractor found
     */
    public Set<String> getProvidedVariables(Class<? extends Event> eventType) {
        EventDataExtractor<? extends Event> extractor = extractors.get(eventType);
        return extractor != null ? extractor.getProvidedVariables() : Set.of();
    }
    
    /**
     * Gets variable descriptions for a specific event type
     * @param eventType The event type
     * @return Map of variable names to descriptions, or empty map if no extractor found
     */
    public Map<String, String> getVariableDescriptions(Class<? extends Event> eventType) {
        EventDataExtractor<? extends Event> extractor = extractors.get(eventType);
        return extractor != null ? extractor.getVariableDescriptions() : Map.of();
    }
    
    /**
     * Gets all registered event types
     * @return Set of all registered event types
     */
    public Set<Class<? extends Event>> getRegisteredEventTypes() {
        return extractors.keySet();
    }
    
    /**
     * Gets the total number of registered extractors
     * @return Number of registered extractors
     */
    public int getExtractorCount() {
        return extractors.size();
    }
}