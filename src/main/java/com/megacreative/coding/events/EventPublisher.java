package com.megacreative.coding.events;

/**
 * Interface for components that publish events in the event-driven architecture.
 * Publishers can send events to the event dispatcher which routes them to subscribers.
 */
public interface EventPublisher {
    
    /**
     * Publishes an event to the event system.
     * 
     * @param event The event to publish
     */
    void publishEvent(CustomEvent event);
    
    /**
     * Publishes an event with associated data to the event system.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     */
    void publishEvent(String eventName, java.util.Map<String, com.megacreative.coding.values.DataValue> eventData);
}