package com.megacreative.coding.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Central event dispatcher that routes events between publishers and subscribers.
 * This is the core of the event-driven architecture.
 */
public class EventDispatcher {
    private static final Logger log = Logger.getLogger(EventDispatcher.class.getName());
    
    private final MegaCreative plugin;
    
    
    private final Map<String, List<EventSubscriber>> subscribers = new ConcurrentHashMap<>();
    
    
    private final Map<EventSubscriber, Set<String>> subscriberEvents = new ConcurrentHashMap<>();
    
    public EventDispatcher(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Registers a subscriber for one or more events.
     * 
     * @param subscriber The subscriber to register
     */
    public void registerSubscriber(EventSubscriber subscriber) {
        String[] events = subscriber.getSubscribedEvents();
        if (events == null) return;
        
        Set<String> eventSet = new HashSet<>();
        for (String eventName : events) {
            if (eventName == null) continue;
            
            subscribers.computeIfAbsent(eventName, k -> new CopyOnWriteArrayList<>()).add(subscriber);
            eventSet.add(eventName);
        }
        
        subscriberEvents.put(subscriber, eventSet);
        log.fine("Registered subscriber for events: " + Arrays.toString(events));
    }
    
    /**
     * Unregisters a subscriber from all events.
     * 
     * @param subscriber The subscriber to unregister
     */
    public void unregisterSubscriber(EventSubscriber subscriber) {
        Set<String> events = subscriberEvents.remove(subscriber);
        if (events == null) return;
        
        for (String eventName : events) {
            List<EventSubscriber> eventSubscribers = subscribers.get(eventName);
            if (eventSubscribers != null) {
                eventSubscribers.remove(subscriber);
            }
        }
        
        log.fine("Unregistered subscriber from events: " + events);
    }
    
    /**
     * Dispatches an event to all registered subscribers.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     * @param source The player that triggered the event (can be null)
     * @param worldName The world where the event occurred
     */
    public void dispatchEvent(String eventName, Map<String, DataValue> eventData, Player source, String worldName) {
        List<EventSubscriber> eventSubscribers = subscribers.get(eventName);
        if (eventSubscribers == null || eventSubscribers.isEmpty()) {
            return;
        }
        
        
        List<EventSubscriber> sortedSubscribers = new ArrayList<>(eventSubscribers);
        sortedSubscribers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        
        
        for (EventSubscriber subscriber : sortedSubscribers) {
            try {
                if (subscriber.shouldHandleEvent(eventName, eventData, source, worldName)) {
                    subscriber.handleEvent(eventName, eventData, source, worldName);
                }
            } catch (Exception e) {
                log.warning("Error in event subscriber: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        log.fine("Dispatched event '" + eventName + "' to " + sortedSubscribers.size() + " subscribers");
    }
    
    /**
     * Gets the number of subscribers for a specific event.
     * 
     * @param eventName The event name
     * @return The number of subscribers
     */
    public int getSubscriberCount(String eventName) {
        List<EventSubscriber> eventSubscribers = subscribers.get(eventName);
        return eventSubscribers != null ? eventSubscribers.size() : 0;
    }
    
    /**
     * Gets all registered event names.
     * 
     * @return A set of all event names that have subscribers
     */
    public Set<String> getRegisteredEvents() {
        return new HashSet<>(subscribers.keySet());
    }
}