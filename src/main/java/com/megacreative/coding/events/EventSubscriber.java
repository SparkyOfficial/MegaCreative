package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import java.util.Map;

/**
 * Interface for components that subscribe to events in the event-driven architecture.
 * Subscribers can register to receive notifications when specific events occur.
 */
public interface EventSubscriber {
    
    /**
     * Handles an event that has been published.
     * 
     * @param event The event that occurred
     * @param eventData The data associated with the event
     * @param source The player that triggered the event (can be null)
     * @param worldName The world where the event occurred
     */
    void handleEvent(String eventName, Map<String, DataValue> eventData, Player source, String worldName);
    
    /**
     * Gets the event names this subscriber is interested in.
     * 
     * @return An array of event names to subscribe to
     */
    String[] getSubscribedEvents();
    
    /**
     * Gets the priority of this subscriber for event handling.
     * Higher priority subscribers are called first.
     * 
     * @return The priority level (higher numbers = higher priority)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Determines if this subscriber should handle the event based on additional criteria.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     * @param source The player that triggered the event (can be null)
     * @param worldName The world where the event occurred
     * @return true if this subscriber should handle the event, false otherwise
     */
    default boolean shouldHandleEvent(String eventName, Map<String, DataValue> eventData, Player source, String worldName) {
        return true;
    }
}