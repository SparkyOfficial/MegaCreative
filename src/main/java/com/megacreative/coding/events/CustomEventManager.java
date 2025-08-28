package com.megacreative.coding.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import lombok.extern.java.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * Manages custom events - registration, triggering, and handling
 */
@Log
public class CustomEventManager implements Listener {
    
    private final MegaCreative plugin;
    
    // Event definitions
    private final Map<String, CustomEvent> eventDefinitions = new ConcurrentHashMap<>();
    
    // Event handlers - maps event name to list of handler blocks
    private final Map<String, List<EventHandler>> eventHandlers = new ConcurrentHashMap<>();
    
    // Global event handlers (work across all worlds)
    private final Map<String, List<EventHandler>> globalEventHandlers = new ConcurrentHashMap<>();
    
    // Event execution history for debugging
    private final Map<String, List<EventExecution>> executionHistory = new ConcurrentHashMap<>();
    
    // Event categories for organization
    private final Map<String, Set<String>> eventCategories = new ConcurrentHashMap<>();
    
    public CustomEventManager(MegaCreative plugin) {
        this.plugin = plugin;
        initializeBuiltInEvents();
    }
    
    /**
     * Registers a custom event definition
     */
    public void registerEvent(CustomEvent event) {
        eventDefinitions.put(event.getName(), event);
        eventHandlers.put(event.getName(), new CopyOnWriteArrayList<>());
        
        if (event.isGlobal()) {
            globalEventHandlers.put(event.getName(), new CopyOnWriteArrayList<>());
        }
        
        // Add to category
        String category = event.getCategory();
        eventCategories.computeIfAbsent(category, k -> new HashSet<>()).add(event.getName());
        
        log.info("Registered custom event: " + event.getName() + " (" + event.getSignature() + ")");
    }
    
    /**
     * Unregisters a custom event
     */
    public void unregisterEvent(String eventName) {
        CustomEvent event = eventDefinitions.remove(eventName);
        if (event != null) {
            eventHandlers.remove(eventName);
            globalEventHandlers.remove(eventName);
            
            // Remove from category
            String category = event.getCategory();
            Set<String> categoryEvents = eventCategories.get(category);
            if (categoryEvents != null) {
                categoryEvents.remove(eventName);
                if (categoryEvents.isEmpty()) {
                    eventCategories.remove(category);
                }
            }
            
            log.info("Unregistered custom event: " + eventName);
        }
    }
    
    /**
     * Registers an event handler (code block that responds to events)
     */
    public void registerEventHandler(String eventName, EventHandler handler) {
        if (!eventDefinitions.containsKey(eventName)) {
            throw new IllegalArgumentException("Event not defined: " + eventName);
        }
        
        CustomEvent event = eventDefinitions.get(eventName);
        
        if (event.isGlobal() || handler.isGlobal()) {
            globalEventHandlers.computeIfAbsent(eventName, k -> new CopyOnWriteArrayList<>()).add(handler);
        } else {
            eventHandlers.computeIfAbsent(eventName, k -> new CopyOnWriteArrayList<>()).add(handler);
        }
        
        log.fine("Registered event handler for: " + eventName + " (priority: " + handler.getPriority() + ")");
    }
    
    /**
     * Unregisters an event handler
     */
    public void unregisterEventHandler(String eventName, EventHandler handler) {
        List<EventHandler> handlers = eventHandlers.get(eventName);
        if (handlers != null) {
            handlers.remove(handler);
        }
        
        List<EventHandler> globalHandlers = globalEventHandlers.get(eventName);
        if (globalHandlers != null) {
            globalHandlers.remove(handler);
        }
    }
    
    /**
     * Triggers a custom event
     */
    public void triggerEvent(String eventName, Map<String, DataValue> eventData, Player source, String worldName) {
        CustomEvent event = eventDefinitions.get(eventName);
        if (event == null) {
            throw new IllegalArgumentException("Event not defined: " + eventName);
        }
        
        try {
            // Validate and prepare event data
            event.validateEventData(eventData);
            Map<String, DataValue> effectiveData = event.prepareEventData(eventData);
            
            // Create event execution record
            EventExecution execution = new EventExecution(eventName, effectiveData, source, worldName);
            
            // Get applicable handlers
            List<EventHandler> applicableHandlers = new ArrayList<>();
            
            // Add global handlers
            List<EventHandler> globalHandlers = globalEventHandlers.get(eventName);
            if (globalHandlers != null) {
                applicableHandlers.addAll(globalHandlers);
            }
            
            // Add world-specific handlers
            if (!event.isGlobal()) {
                List<EventHandler> worldHandlers = eventHandlers.get(eventName);
                if (worldHandlers != null) {
                    applicableHandlers.addAll(worldHandlers.stream()
                        .filter(h -> h.getWorldName() == null || h.getWorldName().equals(worldName))
                        .toList());
                }
            }
            
            // Sort handlers by priority (highest first)
            applicableHandlers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
            
            // Execute handlers
            int executedCount = 0;
            for (EventHandler handler : applicableHandlers) {
                try {
                    if (handler.canHandle(source, worldName, effectiveData)) {
                        handler.handle(effectiveData, source, worldName);
                        executedCount++;
                        
                        // Stop if this is a one-time event and it was handled
                        if (event.isOneTime()) {
                            unregisterEvent(eventName);
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error executing event handler for " + eventName, e);
                }
            }
            
            execution.setHandlersExecuted(executedCount);
            execution.setExecutionTime(System.currentTimeMillis() - execution.getTriggeredTime());
            
            // Store execution history
            executionHistory.computeIfAbsent(eventName, k -> new ArrayList<>()).add(execution);
            
            log.fine("Triggered event: " + eventName + " (executed " + executedCount + " handlers)");
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to trigger event: " + eventName, e);
        }
    }
    
    /**
     * Gets all registered events
     */
    public Map<String, CustomEvent> getEvents() {
        return new HashMap<>(eventDefinitions);
    }
    
    /**
     * Gets events by category
     */
    public Set<String> getEventsByCategory(String category) {
        return eventCategories.getOrDefault(category, new HashSet<>());
    }
    
    /**
     * Gets all event categories
     */
    public Set<String> getCategories() {
        return new HashSet<>(eventCategories.keySet());
    }
    
    /**
     * Gets execution history for an event
     */
    public List<EventExecution> getExecutionHistory(String eventName) {
        return executionHistory.getOrDefault(eventName, new ArrayList<>());
    }
    
    /**
     * Clears execution history
     */
    public void clearExecutionHistory(String eventName) {
        if (eventName == null) {
            executionHistory.clear();
        } else {
            executionHistory.remove(eventName);
        }
    }
    
    /**
     * Creates a new event handler from a code block
     */
    public EventHandler createEventHandler(CodeBlock handlerBlock, Player player, String worldName, int priority) {
        return new EventHandler(handlerBlock, player, worldName, priority);
    }
    
    /**
     * Initializes built-in events
     */
    private void initializeBuiltInEvents() {
        // Player events
        registerEvent(new CustomEvent("playerConnect", "system")
            .addDataField("player", Player.class, true, "Player who connected")
            .addDataField("firstTime", Boolean.class, false, "Is this the player's first time?")
            .setGlobal(true)
            .addTag("player")
            .addTag("connection"));
            
        registerEvent(new CustomEvent("playerDisconnect", "system")
            .addDataField("player", Player.class, true, "Player who disconnected")
            .addDataField("reason", String.class, false, "Disconnect reason")
            .setGlobal(true)
            .addTag("player")
            .addTag("connection"));
            
        // Script events
        registerEvent(new CustomEvent("scriptComplete", "system")
            .addDataField("scriptName", String.class, true, "Name of completed script")
            .addDataField("executionTime", Long.class, false, "Execution time in milliseconds")
            .addDataField("success", Boolean.class, true, "Whether script completed successfully")
            .addTag("script")
            .addTag("execution"));
            
        // Custom user events
        registerEvent(new CustomEvent("userMessage", "system")
            .addDataField("message", String.class, true, "Message content")
            .addDataField("sender", Player.class, false, "Message sender")
            .addDataField("priority", Integer.class, false, "Message priority")
            .addTag("communication"));
    }
    
    /**
     * Handle Minecraft events to trigger custom events
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        data.put("player", DataValue.fromObject(event.getPlayer()));
        data.put("firstTime", DataValue.fromObject(!event.getPlayer().hasPlayedBefore()));
        
        triggerEvent("playerConnect", data, event.getPlayer(), event.getPlayer().getWorld().getName());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        data.put("player", DataValue.fromObject(event.getPlayer()));
        data.put("reason", DataValue.fromObject(event.getQuitMessage()));
        
        triggerEvent("playerDisconnect", data, event.getPlayer(), event.getPlayer().getWorld().getName());
    }
    
    /**
     * Event handler implementation
     */
    public static class EventHandler {
        private final CodeBlock handlerBlock;
        private final UUID playerId;
        private final String worldName;
        private final int priority;
        private final boolean isGlobal;
        
        public EventHandler(CodeBlock handlerBlock, Player player, String worldName, int priority) {
            this.handlerBlock = handlerBlock;
            this.playerId = player != null ? player.getUniqueId() : null;
            this.worldName = worldName;
            this.priority = priority;
            this.isGlobal = worldName == null;
        }
        
        public boolean canHandle(Player source, String sourceWorld, Map<String, DataValue> eventData) {
            // Global handlers can handle any event
            if (isGlobal) return true;
            
            // Check world restriction
            if (worldName != null && !worldName.equals(sourceWorld)) return false;
            
            // Check player restriction
            if (playerId != null && source != null && !playerId.equals(source.getUniqueId())) return false;
            
            return true;
        }
        
        public void handle(Map<String, DataValue> eventData, Player source, String sourceWorld) {
            // Execute the handler block with event data as variables
            // This would integrate with the existing script execution system
            if (handlerBlock != null && handlerBlock.getPlugin() != null) {
                // Set event data as local variables for the handler
                for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                    // Set as local variable in the handler's execution context
                    // This would use the VariableManager to set local scope variables
                }
                
                // Execute the handler block
                try {
                    handlerBlock.execute(source);
                } catch (Exception e) {
                    // Log error but don't propagate to avoid breaking other handlers
                }
            }
        }
        
        // Getters
        public CodeBlock getHandlerBlock() { return handlerBlock; }
        public UUID getPlayerId() { return playerId; }
        public String getWorldName() { return worldName; }
        public int getPriority() { return priority; }
        public boolean isGlobal() { return isGlobal; }
    }
    
    /**
     * Event execution record for debugging and analytics
     */
    @Data
    public static class EventExecution {
        private final String eventName;
        private final Map<String, DataValue> eventData;
        private final UUID sourcePlayerId;
        private final String worldName;
        private final long triggeredTime;
        private int handlersExecuted = 0;
        private long executionTime = 0;
        
        public EventExecution(String eventName, Map<String, DataValue> eventData, Player source, String worldName) {
            this.eventName = eventName;
            this.eventData = new HashMap<>(eventData);
            this.sourcePlayerId = source != null ? source.getUniqueId() : null;
            this.worldName = worldName;
            this.triggeredTime = System.currentTimeMillis();
        }
    }
}