package com.megacreative.coding.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
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
public class CustomEventManager implements Listener {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(CustomEventManager.class.getName());
    
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
    
    // Advanced event triggers
    private final Map<String, AdvancedEventTrigger> advancedTriggers = new ConcurrentHashMap<>();
    
    // Scheduled triggers
    private final Map<String, ScheduledTrigger> scheduledTriggers = new ConcurrentHashMap<>();
    
    // Event correlation engine
    private final EventCorrelationEngine correlationEngine;
    
    public CustomEventManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.correlationEngine = new EventCorrelationEngine(this);
        initializeBuiltInEvents();
        startCorrelationCleanupTask();
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
        CustomEvent event = getEventByName(eventName);
        if (event == null) {
            throw new IllegalArgumentException("Event not defined: " + eventName);
        }
        
        // Check if event is abstract
        if (event.isAbstract()) {
            throw new IllegalArgumentException("Cannot trigger abstract event: " + eventName);
        }
        
        try {
            // Validate and prepare event data
            event.validateEventData(eventData);
            Map<String, DataValue> effectiveData = event.prepareEventData(eventData);
            
            // Process event through correlation engine
            correlationEngine.processEvent(eventName, effectiveData, source, worldName);
            
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
                    log.severe("Error executing event handler: " + e.getMessage());
                    e.printStackTrace();
                    // Log error but don't propagate to avoid breaking other handlers
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
     * Triggers a custom event with filtering
     */
    public void triggerEvent(String eventName, Map<String, DataValue> eventData, Player source, String worldName, 
                           java.util.function.Predicate<EventHandler> filter) {
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
            
            // Apply custom filter if provided
            if (filter != null) {
                applicableHandlers.removeIf(handler -> !filter.test(handler));
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
     * Transforms event data using a mapping function
     */
    public Map<String, DataValue> transformEventData(Map<String, DataValue> originalData, 
                                                   Map<String, java.util.function.Function<DataValue, DataValue>> transformations) {
        Map<String, DataValue> transformedData = new HashMap<>(originalData);
        
        for (Map.Entry<String, java.util.function.Function<DataValue, DataValue>> entry : transformations.entrySet()) {
            String fieldName = entry.getKey();
            java.util.function.Function<DataValue, DataValue> transformer = entry.getValue();
            
            if (originalData.containsKey(fieldName)) {
                DataValue originalValue = originalData.get(fieldName);
                DataValue transformedValue = transformer.apply(originalValue);
                transformedData.put(fieldName, transformedValue);
            }
        }
        
        return transformedData;
    }
    
    /**
     * Merges multiple event data maps
     */
    public Map<String, DataValue> mergeEventData(Map<String, DataValue>... dataMaps) {
        Map<String, DataValue> merged = new HashMap<>();
        for (Map<String, DataValue> dataMap : dataMaps) {
            merged.putAll(dataMap);
        }
        return merged;
    }
    
    /**
     * Creates a data filter for event handlers
     */
    public java.util.function.Predicate<EventHandler> createPlayerFilter(UUID playerId) {
        return handler -> handler.getPlayerId() == null || handler.getPlayerId().equals(playerId);
    }
    
    /**
     * Creates a world filter for event handlers
     */
    public java.util.function.Predicate<EventHandler> createWorldFilter(String worldName) {
        return handler -> handler.getWorldName() == null || handler.getWorldName().equals(worldName);
    }
    
    /**
     * Creates a combined filter for event handlers
     */
    public java.util.function.Predicate<EventHandler> createCombinedFilter(
            java.util.function.Predicate<EventHandler>... filters) {
        return handler -> {
            for (java.util.function.Predicate<EventHandler> filter : filters) {
                if (!filter.test(handler)) {
                    return false;
                }
            }
            return true;
        };
    }
    
    /**
     * Initializes built-in events
     */
    private void initializeBuiltInEvents() {
        // Player events
        CustomEvent playerConnectEvent = new CustomEvent("playerConnect", "system")
            .addDataField("player", Player.class, true, "Player who connected")
            .addDataField("firstTime", Boolean.class, false, "Is this the player's first time?");
        playerConnectEvent.setGlobal(true);
        playerConnectEvent.addTag("player");
        playerConnectEvent.addTag("connection");
        registerEvent(playerConnectEvent);
            
        CustomEvent playerDisconnectEvent = new CustomEvent("playerDisconnect", "system")
            .addDataField("player", Player.class, true, "Player who disconnected")
            .addDataField("reason", String.class, false, "Disconnect reason");
        playerDisconnectEvent.setGlobal(true);
        playerDisconnectEvent.addTag("player");
        playerDisconnectEvent.addTag("connection");
        registerEvent(playerDisconnectEvent);
            
        // Script events
        CustomEvent scriptCompleteEvent = new CustomEvent("scriptComplete", "system")
            .addDataField("scriptName", String.class, true, "Name of completed script")
            .addDataField("executionTime", Long.class, false, "Execution time in milliseconds")
            .addDataField("success", Boolean.class, true, "Whether script completed successfully");
        scriptCompleteEvent.addTag("script");
        scriptCompleteEvent.addTag("execution");
        registerEvent(scriptCompleteEvent);
            
        // Custom user events
        CustomEvent userMessageEvent = new CustomEvent("userMessage", "system")
            .addDataField("message", String.class, true, "Message content")
            .addDataField("sender", Player.class, false, "Message sender")
            .addDataField("priority", Integer.class, false, "Message priority");
        userMessageEvent.addTag("communication");
        registerEvent(userMessageEvent);
    }
    
    /**
     * Handle Minecraft events to trigger custom events
     */
    @org.bukkit.event.EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        data.put("player", DataValue.fromObject(event.getPlayer()));
        data.put("firstTime", DataValue.fromObject(!event.getPlayer().hasPlayedBefore()));
        
        triggerEvent("playerConnect", data, event.getPlayer(), event.getPlayer().getWorld().getName());
    }
    
    @org.bukkit.event.EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        data.put("player", DataValue.fromObject(event.getPlayer()));
        data.put("reason", DataValue.fromObject(event.getQuitMessage()));
        
        triggerEvent("playerDisconnect", data, event.getPlayer(), event.getPlayer().getWorld().getName());
    }
    
    /**
     * Gets the event correlation engine
     */
    public EventCorrelationEngine getCorrelationEngine() {
        return correlationEngine;
    }
    
    /**
     * Starts the correlation cleanup task
     */
    private void startCorrelationCleanupTask() {
        // Use Bukkit's scheduler instead of creating direct threads
        com.megacreative.MegaCreative plugin = com.megacreative.MegaCreative.getInstance();
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            correlationEngine.cleanupExpiredInstances();
        }, 1200L, 1200L); // Run every minute (1200 ticks = 60 seconds)
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
            // Called from lines 183 and 262 in the same file
            if (handlerBlock != null) {
                // Set event data as local variables for the handler
                for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                    // Set as local variable in the handler's execution context
                    // This would use the VariableManager to set local scope variables
                }
                
                // Execute the handler block
                try {
                    // This would need integration with the execution system
                    // handlerBlock.execute(source);
                    // TODO: Implement actual handler execution
                    // For now, we're just logging that the handler was called
                    com.megacreative.MegaCreative.getInstance().getLogger().info(
                        "EventHandler called for player " + (source != null ? source.getName() : "unknown") + 
                        " in world " + sourceWorld
                    );
                } catch (Exception e) {
                    // Log error but don't propagate to avoid breaking other handlers
                    com.megacreative.MegaCreative.getInstance().getLogger().warning(
                        "Error in EventHandler: " + e.getMessage()
                    );
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
        
        // Getters
        public String getEventName() { return eventName; }
        public Map<String, DataValue> getEventData() { return eventData; }
        public UUID getSourcePlayerId() { return sourcePlayerId; }
        public String getWorldName() { return worldName; }
        public long getTriggeredTime() { return triggeredTime; }
        public int getHandlersExecuted() { return handlersExecuted; }
        public long getExecutionTime() { return executionTime; }
        
        // Setters
        public void setHandlersExecuted(int handlersExecuted) { this.handlersExecuted = handlersExecuted; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    }
    
    /**
     * Registers an advanced event trigger
     */
    public void registerAdvancedTrigger(AdvancedEventTrigger trigger) {
        advancedTriggers.put(trigger.getTriggerId(), trigger);
        log.info("Registered advanced event trigger: " + trigger.getTriggerId() + " for event: " + trigger.getEventName());
    }
    
    /**
     * Unregisters an advanced event trigger
     */
    public void unregisterAdvancedTrigger(String triggerId) {
        AdvancedEventTrigger trigger = advancedTriggers.remove(triggerId);
        if (trigger != null) {
            log.info("Unregistered advanced event trigger: " + triggerId);
        }
    }
    
    /**
     * Gets an advanced event trigger by ID
     */
    public AdvancedEventTrigger getAdvancedTrigger(String triggerId) {
        return advancedTriggers.get(triggerId);
    }
    
    /**
     * Gets all advanced triggers for an event
     */
    public List<AdvancedEventTrigger> getAdvancedTriggersForEvent(String eventName) {
        return advancedTriggers.values().stream()
            .filter(trigger -> trigger.getEventName().equals(eventName))
            .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
    }
    
    /**
     * Schedules an event trigger
     */
    public void scheduleTrigger(String triggerId, long delayMs, Player player, String world) {
        ScheduledTrigger scheduled = new ScheduledTrigger(triggerId, delayMs, player, world);
        scheduledTriggers.put(triggerId, scheduled);
        scheduled.schedule(this);
    }
    
    /**
     * Cancels a scheduled trigger
     */
    public void cancelScheduledTrigger(String triggerId) {
        ScheduledTrigger scheduled = scheduledTriggers.remove(triggerId);
        if (scheduled != null) {
            scheduled.cancel();
        }
    }
    
    /**
     * Executes an advanced trigger
     */
    public void executeAdvancedTrigger(String triggerId, Player player, String world) {
        AdvancedEventTrigger trigger = advancedTriggers.get(triggerId);
        if (trigger != null) {
            trigger.execute(this, player, world);
        }
    }
    
    /**
     * Scheduled trigger wrapper
     */
    private static class ScheduledTrigger {
        private final String triggerId;
        private final long delayMs;
        private final UUID playerId;
        private final String worldName;
        private boolean cancelled = false;
        private int taskId = -1; // Bukkit task ID
        
        public ScheduledTrigger(String triggerId, long delayMs, Player player, String worldName) {
            this.triggerId = triggerId;
            this.delayMs = delayMs;
            this.playerId = player != null ? player.getUniqueId() : null;
            this.worldName = worldName;
        }
        
        public void schedule(CustomEventManager manager) {
            // Use Bukkit's scheduler instead of creating direct threads
            com.megacreative.MegaCreative plugin = com.megacreative.MegaCreative.getInstance();
            this.taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (!cancelled) {
                    manager.executeAdvancedTrigger(triggerId, 
                        playerId != null ? plugin.getServer().getPlayer(playerId) : null, 
                        worldName);
                }
            }, delayMs / 50); // Convert milliseconds to ticks (1 tick = 50ms)
        }
        
        public void cancel() {
            cancelled = true;
            if (taskId != -1) {
                com.megacreative.MegaCreative plugin = com.megacreative.MegaCreative.getInstance();
                plugin.getServer().getScheduler().cancelTask(taskId);
            }
        }
    }
    
    /**
     * Gets an event by name, checking aliases and inheritance
     */
    public CustomEvent getEventByName(String eventName) {
        // Direct match
        if (eventDefinitions.containsKey(eventName)) {
            return eventDefinitions.get(eventName);
        }
        
        // Check aliases
        for (CustomEvent event : eventDefinitions.values()) {
            if (event.getAliases().contains(eventName)) {
                return event;
            }
        }
        
        return null;
    }
    
    /**
     * Gets events by tag
     */
    public List<CustomEvent> getEventsByTag(String tag) {
        List<CustomEvent> result = new ArrayList<>();
        for (CustomEvent event : eventDefinitions.values()) {
            if (event.hasTag(tag)) {
                result.add(event);
            }
        }
        return result;
    }
    
    /**
     * Gets events by metadata
     */
    /**
     * Gets events by metadata
     * @param key The metadata key to search for
     * @param value The expected metadata value
     * @return List of events with matching metadata
     */
    public List<CustomEvent> getEventsByMetadata(String key, Object value) {
        List<CustomEvent> result = new ArrayList<>();
        for (CustomEvent event : eventDefinitions.values()) {
            Object metadataValue = event.getMetadata(key, Object.class);
            if (metadataValue != null && metadataValue.equals(value)) {
                result.add(event);
            }
        }
        return result;
    }
}
