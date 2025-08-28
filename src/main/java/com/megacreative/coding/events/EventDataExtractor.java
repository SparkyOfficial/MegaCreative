package com.megacreative.coding.events;

import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import org.bukkit.event.Event;

import java.util.Map;
import java.util.Set;

/**
 * Unified interface for extracting data from Bukkit events
 * Provides consistent and flexible event data access across the visual programming system
 */
public interface EventDataExtractor<T extends Event> {
    
    /**
     * Gets the event type this extractor handles
     * @return The Class of the event type
     */
    Class<T> getEventType();
    
    /**
     * Extracts all relevant data from an event as DataValue objects
     * @param event The event to extract data from
     * @return Map of variable names to DataValue objects
     */
    Map<String, DataValue> extractData(T event);
    
    /**
     * Applies the extracted data to an execution context
     * This method sets variables in the context using the extracted data
     * @param event The event to extract data from
     * @param context The execution context to populate
     */
    default void populateContext(T event, ExecutionContext context) {
        Map<String, DataValue> data = extractData(event);
        for (Map.Entry<String, DataValue> entry : data.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue().getValue());
        }
    }
    
    /**
     * Gets the set of variable names that this extractor provides
     * @return Set of variable names
     */
    Set<String> getProvidedVariables();
    
    /**
     * Gets a description of what each variable represents
     * @return Map of variable names to their descriptions
     */
    Map<String, String> getVariableDescriptions();
    
    /**
     * Checks if this extractor can handle the given event
     * @param event The event to check
     * @return true if this extractor can handle the event
     */
    default boolean canHandle(Event event) {
        return getEventType().isInstance(event);
    }
}