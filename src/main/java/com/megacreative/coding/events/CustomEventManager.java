package com.megacreative.coding.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages custom events - registration, triggering, and handling
 * Now integrated with the new event-driven architecture
 * 
 * This class provides a comprehensive event management system that supports:
 * - Custom event definition and registration
 * - Event handler registration and execution
 * - Global and world-specific event handling
 * - Event filtering and prioritization
 * - Event correlation and advanced triggers
 * - Scheduled event execution
 * - Event execution history and debugging
 * - Event categories and metadata
 */
public class CustomEventManager implements Listener, EventPublisher, EventSubscriber {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(CustomEventManager.class.getName());
    
    private final MegaCreative plugin;
    private final EventDispatcher eventDispatcher;
    
    
    private final Map<String, CustomEvent> eventDefinitions = new ConcurrentHashMap<>();
    
    
    private final Map<String, List<EventHandler>> eventHandlers = new ConcurrentHashMap<>();
    
    
    private final Map<String, List<EventHandler>> globalEventHandlers = new ConcurrentHashMap<>();
    
    
    private final Map<String, List<EventExecution>> executionHistory = new ConcurrentHashMap<>();
    
    
    private final Map<String, Set<String>> eventCategories = new ConcurrentHashMap<>();
    
    
    private final Map<String, AdvancedEventTrigger> advancedTriggers = new ConcurrentHashMap<>();
    
    
    private final Map<String, ScheduledTrigger> scheduledTriggers = new ConcurrentHashMap<>();
    
    
    private final EventCorrelationEngine correlationEngine;
    
    
    private final RegionDetectionSystem regionDetectionSystem;
    
    public CustomEventManager(MegaCreative plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.eventDispatcher = new EventDispatcher(plugin);
        this.correlationEngine = new EventCorrelationEngine(this);
        this.regionDetectionSystem = new RegionDetectionSystem(plugin, this);
        initializeBuiltInEvents();
        startTickTask();
        startCorrelationCleanupTask();
    }
    
    /**
     * Registers a custom event definition
     * 
     * @param event the event to register
     * @throws IllegalArgumentException if event or event name is null
     */
    public void registerEvent(CustomEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        String eventName = event.getName();
        if (eventName == null) {
            throw new IllegalArgumentException("Event name cannot be null");
        }
        
        eventDefinitions.put(eventName, event);
        eventHandlers.putIfAbsent(eventName, new CopyOnWriteArrayList<>());
        
        if (event.isGlobal()) {
            globalEventHandlers.putIfAbsent(eventName, new CopyOnWriteArrayList<>());
        }
        
        
        String category = event.getCategory();
        if (category != null) {
            eventCategories.computeIfAbsent(category, k -> ConcurrentHashMap.newKeySet()).add(eventName);
        }
        
        log.info("Registered custom event: " + eventName + " (" + event.getSignature() + ")");
    }
    
    /**
     * Unregisters a custom event
     * 
     * @param eventName the name of the event to unregister
     */
    public void unregisterEvent(String eventName) {
        if (eventName == null) {
            return;
        }
        
        CustomEvent event = eventDefinitions.remove(eventName);
        if (event != null) {
            eventHandlers.remove(eventName);
            globalEventHandlers.remove(eventName);
            
            
            String category = event.getCategory();
            if (category != null) {
                Set<String> categoryEvents = eventCategories.get(category);
                if (categoryEvents != null) {
                    categoryEvents.remove(eventName);
                    if (categoryEvents.isEmpty()) {
                        eventCategories.remove(category);
                    }
                }
            }
            
            log.info("Unregistered custom event: " + eventName);
        }
    }
    
    /**
     * Clears all registered events and handlers
     */
    public void clearAllEvents() {
        eventDefinitions.clear();
        eventHandlers.clear();
        globalEventHandlers.clear();
        eventCategories.clear();
        executionHistory.clear();
        advancedTriggers.clear();
        scheduledTriggers.clear();
        
        log.info("Cleared all events and handlers");
    }
    
    /**
     * Registers an event handler (code block that responds to events)
     * 
     * @param eventName the name of the event to handle
     * @param handler the event handler to register
     * @throws IllegalArgumentException if event name or handler is null, or if event is not defined
     */
    public void registerEventHandler(String eventName, EventHandler handler) {
        if (eventName == null) {
            throw new IllegalArgumentException("Event name cannot be null");
        }
        
        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null");
        }
        
        if (!eventDefinitions.containsKey(eventName)) {
            throw new IllegalArgumentException("Event not defined: " + eventName);
        }
        
        CustomEvent event = eventDefinitions.get(eventName);
        if (event == null) {
            throw new IllegalArgumentException("Event definition is null for: " + eventName);
        }
        
        if (event.isGlobal() || handler.isGlobal()) {
            globalEventHandlers.computeIfAbsent(eventName, k -> new CopyOnWriteArrayList<>()).add(handler);
        } else {
            eventHandlers.computeIfAbsent(eventName, k -> new CopyOnWriteArrayList<>()).add(handler);
        }
        
        log.fine("Registered event handler for: " + eventName + " (priority: " + handler.getPriority() + ")");
    }
    
    /**
     * Unregisters an event handler
     * 
     * @param eventName the name of the event
     * @param handler the handler to unregister
     */
    public void unregisterEventHandler(String eventName, EventHandler handler) {
        if (eventName == null || handler == null) {
            return;
        }
        
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
     * 
     * @param eventName the name of the event to trigger
     * @param eventData the data associated with the event
     * @param source the player that triggered the event (can be null)
     * @param worldName the world where the event occurred
     */
    public void triggerEvent(String eventName, Map<String, DataValue> eventData, Player source, String worldName) {
        triggerEvent(eventName, eventData, source, worldName, null);
    }
    
    /**
     * Triggers a custom event with filtering
     * 
     * @param eventName the name of the event to trigger
     * @param eventData the data associated with the event
     * @param source the player that triggered the event (can be null)
     * @param worldName the world where the event occurred
     * @param filter optional filter for event handlers
     */
    public void triggerEvent(String eventName, Map<String, DataValue> eventData, Player source, String worldName, 
                           Predicate<EventHandler> filter) {
        CustomEvent event = getEventByName(eventName);
        if (event == null) {
            throw new IllegalArgumentException("Event not defined: " + eventName);
        }
        
        
        if (event.isAbstract()) {
            throw new IllegalArgumentException("Cannot trigger abstract event: " + eventName);
        }
        
        try {
            
            event.validateEventData(eventData);
            Map<String, DataValue> effectiveData = event.prepareEventData(eventData);
            
            
            correlationEngine.processEvent(eventName, effectiveData, source, worldName);
            
            
            EventExecution execution = new EventExecution(eventName, effectiveData, source, worldName);
            
            
            List<EventHandler> applicableHandlers = getApplicableEventHandlers(eventName, event, worldName, filter);
            
            
            applicableHandlers.sort(Comparator.comparingInt(EventHandler::getPriority).reversed());
            
            
            int executedCount = executeEventHandlers(applicableHandlers, event, eventName, effectiveData, source, worldName);
            
            execution.setHandlersExecuted(executedCount);
            execution.setExecutionTime(System.currentTimeMillis() - execution.getTriggeredTime());
            
            
            List<EventExecution> history = executionHistory.computeIfAbsent(eventName, k -> new ArrayList<>());
            history.add(execution);
            
            if (history.size() > 1000) {
                history.subList(0, history.size() - 1000).clear();
            }
            
            log.fine("Triggered event: " + eventName + " (executed " + executedCount + " handlers)");
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to trigger event: " + eventName, e);
        }
    }
    
    /**
     * Gets all registered events
     * 
     * @return a copy of all registered events
     */
    public Map<String, CustomEvent> getEvents() {
        return new HashMap<>(eventDefinitions);
    }
    
    /**
     * Gets the number of registered events
     * 
     * @return the count of registered events
     */
    public int getEventCount() {
        return eventDefinitions.size();
    }
    
    /**
     * Gets event statistics
     * 
     * @return a map containing various event statistics
     */
    public Map<String, Object> getEventStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", eventDefinitions.size());
        stats.put("totalCategories", eventCategories.size());
        stats.put("totalHandlers", eventHandlers.size() + globalEventHandlers.size());
        stats.put("totalExecutions", executionHistory.values().stream().mapToInt(List::size).sum());
        stats.put("totalAdvancedTriggers", advancedTriggers.size());
        stats.put("totalScheduledTriggers", scheduledTriggers.size());
        return stats;
    }
    
    /**
     * Checks if an event is registered
     * 
     * @param eventName the name of the event to check
     * @return true if the event is registered, false otherwise
     */
    public boolean isEventRegistered(String eventName) {
        return eventName != null && eventDefinitions.containsKey(eventName);
    }
    
    /**
     * Gets applicable event handlers for an event
     */
    private List<EventHandler> getApplicableEventHandlers(String eventName, CustomEvent event, String worldName, 
                                                        Predicate<EventHandler> filter) {
        List<EventHandler> applicableHandlers = new ArrayList<>();
        
        
        List<EventHandler> globalHandlers = globalEventHandlers.get(eventName);
        if (globalHandlers != null) {
            applicableHandlers.addAll(globalHandlers);
        }
        
        
        if (!event.isGlobal()) {
            List<EventHandler> worldHandlers = eventHandlers.get(eventName);
            if (worldHandlers != null) {
                applicableHandlers.addAll(worldHandlers.stream()
                    .filter(h -> h.getWorldName() == null || h.getWorldName().equals(worldName))
                    .collect(Collectors.toList()));
            }
        }
        
        
        if (filter != null) {
            applicableHandlers.removeIf(handler -> !filter.test(handler));
        }
        
        return applicableHandlers;
    }
    
    /**
     * Executes event handlers for an event
     */
    private int executeEventHandlers(List<EventHandler> handlers, CustomEvent event, String eventName, 
                                   Map<String, DataValue> eventData, Player source, String worldName) {
        int executedCount = 0;
        for (EventHandler handler : handlers) {
            try {
                if (handler.canHandle(source, worldName, eventData)) {
                    handler.handle(eventData, source, worldName);
                    executedCount++;
                    
                    
                    if (event.isOneTime()) {
                        unregisterEvent(eventName);
                        break;
                    }
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Error executing event handler for " + eventName, e);
            }
        }
        return executedCount;
    }
    
    /**
     * Gets events by category
     * 
     * @param category the category to search for
     * @return a set of event names in the category
     */
    public Set<String> getEventsByCategory(String category) {
        Set<String> events = eventCategories.get(category);
        return events != null ? new HashSet<>(events) : Collections.emptySet();
    }
    
    /**
     * Gets all event categories
     * 
     * @return a set of all event categories
     */
    public Set<String> getCategories() {
        return new HashSet<>(eventCategories.keySet());
    }
    
    /**
     * Gets execution history for an event
     * 
     * @param eventName the name of the event
     * @return a list of event executions
     */
    public List<EventExecution> getExecutionHistory(String eventName) {
        List<EventExecution> history = executionHistory.get(eventName);
        return history != null ? new ArrayList<>(history) : Collections.emptyList();
    }
    
    /**
     * Clears execution history
     * 
     * @param eventName the name of the event, or null to clear all history
     */
    public void clearExecutionHistory(String eventName) {
        if (eventName == null) {
            executionHistory.clear();
        } else {
            executionHistory.remove(eventName);
        }
    }
    
    /**
     * Gets all event handlers for an event
     * 
     * @param eventName the name of the event
     * @return a list of event handlers
     */
    public List<EventHandler> getEventHandlers(String eventName) {
        
        List<EventHandler> handlers = eventHandlers.get(eventName);
        return handlers != null ? new ArrayList<>(handlers) : Collections.emptyList();
    }
    
    /**
     * Gets the number of event handlers for an event
     * 
     * @param eventName the name of the event
     * @return the count of event handlers
     */
    public int getEventHandlerCount(String eventName) {
        if (eventName == null) {
            return 0;
        }
        
        List<EventHandler> handlers = eventHandlers.get(eventName);
        List<EventHandler> globalHandlers = globalEventHandlers.get(eventName);
        
        int count = 0;
        if (handlers != null) {
            count += handlers.size();
        }
        if (globalHandlers != null) {
            count += globalHandlers.size();
        }
        
        return count;
    }
    
    /**
     * Gets global event handlers for an event
     * 
     * @param eventName the name of the event
     * @return a list of global event handlers
     */
    public List<EventHandler> getGlobalEventHandlers(String eventName) {
        List<EventHandler> handlers = globalEventHandlers.get(eventName);
        return handlers != null ? new ArrayList<>(handlers) : Collections.emptyList();
    }
    
    /**
     * Creates a new event handler from a code block
     * 
     * @param handlerBlock the code block to execute
     * @param player the player associated with the handler
     * @param worldName the world name restriction
     * @param priority the priority of the handler
     * @return a new EventHandler instance
     */
    public EventHandler createEventHandler(CodeBlock handlerBlock, Player player, String worldName, int priority) {
        return new EventHandler(handlerBlock, player, worldName, priority, plugin);
    }
    
    /**
     * Transforms event data using a mapping function
     * 
     * @param originalData the original event data
     * @param transformations the transformations to apply
     * @return transformed event data
     */
    public Map<String, DataValue> transformEventData(Map<String, DataValue> originalData, 
                                                   Map<String, Function<DataValue, DataValue>> transformations) {
        Map<String, DataValue> transformedData = new HashMap<>(originalData);
        
        transformations.forEach((fieldName, transformer) -> {
            if (originalData.containsKey(fieldName)) {
                DataValue originalValue = originalData.get(fieldName);
                DataValue transformedValue = transformer.apply(originalValue);
                transformedData.put(fieldName, transformedValue);
            }
        });
        
        return transformedData;
    }
    
    /**
     * Merges multiple event data maps
     * 
     * @param dataMaps the data maps to merge
     * @return merged event data
     */
    public Map<String, DataValue> mergeEventData(Map<String, DataValue>... dataMaps) {
        return Arrays.stream(dataMaps)
            .filter(Objects::nonNull)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> replacement
            ));
    }
    
    /**
     * Creates a data filter for event handlers
     * 
     * @param playerId the player ID to filter by
     * @return a predicate for filtering handlers
     */
    public Predicate<EventHandler> createPlayerFilter(UUID playerId) {
        return handler -> handler.getPlayerId() == null || handler.getPlayerId().equals(playerId);
    }
    
    /**
     * Creates a world filter for event handlers
     * 
     * @param worldName the world name to filter by
     * @return a predicate for filtering handlers
     */
    public Predicate<EventHandler> createWorldFilter(String worldName) {
        return handler -> handler.getWorldName() == null || handler.getWorldName().equals(worldName);
    }
    
    /**
     * Creates a combined filter for event handlers
     * 
     * @param filters the filters to combine
     * @return a predicate that combines all filters
     */
    public Predicate<EventHandler> createCombinedFilter(Predicate<EventHandler>... filters) {
        return handler -> Arrays.stream(filters)
            .filter(Objects::nonNull)
            .allMatch(filter -> filter.test(handler));
    }
    
    /**
     * Initializes built-in events
     */
    private void initializeBuiltInEvents() {
        // Player connection events
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
            
        // Player movement event
        CustomEvent playerMoveEvent = new CustomEvent("playerMove", "system")
            .addDataField("player", Player.class, true, "Player who moved")
            .addDataField("from", org.bukkit.Location.class, true, "Previous location")
            .addDataField("to", org.bukkit.Location.class, true, "New location");
        playerMoveEvent.addTag("player");
        playerMoveEvent.addTag("movement");
        registerEvent(playerMoveEvent);
            
        // Player chat event
        CustomEvent playerChatEvent = new CustomEvent("playerChat", "system")
            .addDataField("player", Player.class, true, "Player who sent the message")
            .addDataField("message", String.class, true, "The message content");
        playerChatEvent.addTag("player");
        playerChatEvent.addTag("communication");
        registerEvent(playerChatEvent);
        
        // Player death event
        CustomEvent playerDeathEvent = new CustomEvent("playerDeath", "system")
            .addDataField("player", Player.class, true, "Player who died")
            .addDataField("killer", Player.class, false, "Player who killed (if any)")
            .addDataField("cause", String.class, false, "Cause of death");
        playerDeathEvent.addTag("player");
        playerDeathEvent.addTag("combat");
        registerEvent(playerDeathEvent);
        
        // Player respawn event
        CustomEvent playerRespawnEvent = new CustomEvent("playerRespawn", "system")
            .addDataField("player", Player.class, true, "Player who respawned")
            .addDataField("location", org.bukkit.Location.class, true, "Respawn location");
        playerRespawnEvent.addTag("player");
        playerRespawnEvent.addTag("spawn");
        registerEvent(playerRespawnEvent);
        
        // Player teleport event
        CustomEvent playerTeleportEvent = new CustomEvent("playerTeleport", "system")
            .addDataField("player", Player.class, true, "Player who teleported")
            .addDataField("from", org.bukkit.Location.class, true, "Previous location")
            .addDataField("to", org.bukkit.Location.class, true, "New location");
        playerTeleportEvent.addTag("player");
        playerTeleportEvent.addTag("movement");
        registerEvent(playerTeleportEvent);
        
        // Entity damage event
        CustomEvent entityDamageEvent = new CustomEvent("entityDamage", "system")
            .addDataField("player", Player.class, true, "Player involved in damage")
            .addDataField("damaged", org.bukkit.entity.Entity.class, true, "Entity that was damaged")
            .addDataField("damage", Double.class, true, "Amount of damage");
        entityDamageEvent.addTag("player");
        entityDamageEvent.addTag("combat");
        registerEvent(entityDamageEvent);
        
        // Inventory click event
        CustomEvent inventoryClickEvent = new CustomEvent("inventoryClick", "system")
            .addDataField("player", Player.class, true, "Player who clicked")
            .addDataField("slot", Integer.class, true, "Clicked slot number")
            .addDataField("item", org.bukkit.inventory.ItemStack.class, false, "Item in the slot");
        inventoryClickEvent.addTag("player");
        inventoryClickEvent.addTag("inventory");
        registerEvent(inventoryClickEvent);
        
        // Entity pickup item event
        CustomEvent entityPickupItemEvent = new CustomEvent("entityPickupItem", "system")
            .addDataField("player", Player.class, true, "Player who picked up the item")
            .addDataField("item", org.bukkit.inventory.ItemStack.class, true, "Item that was picked up");
        entityPickupItemEvent.addTag("player");
        entityPickupItemEvent.addTag("inventory");
        registerEvent(entityPickupItemEvent);
        
        // Tick event (global)
        CustomEvent tickEvent = new CustomEvent("tick", "system")
            .addDataField("tickCount", Long.class, true, "Current tick count");
        tickEvent.setGlobal(true);
        tickEvent.addTag("system");
        registerEvent(tickEvent);
            
        // Script completion event
        CustomEvent scriptCompleteEvent = new CustomEvent("scriptComplete", "system")
            .addDataField("scriptName", String.class, true, "Name of completed script")
            .addDataField("executionTime", Long.class, false, "Execution time in milliseconds")
            .addDataField("success", Boolean.class, true, "Whether script completed successfully");
        scriptCompleteEvent.addTag("script");
        scriptCompleteEvent.addTag("execution");
        registerEvent(scriptCompleteEvent);
            
        // User message event
        CustomEvent userMessageEvent = new CustomEvent("userMessage", "system")
            .addDataField("message", String.class, true, "Message content")
            .addDataField("sender", Player.class, false, "Message sender")
            .addDataField("priority", Integer.class, false, "Message priority");
        userMessageEvent.addTag("communication");
        registerEvent(userMessageEvent);
        
        // Region events
        CustomEvent regionEnterEvent = new CustomEvent("regionEnter", "system")
            .addDataField("player", Player.class, true, "Player who entered the region")
            .addDataField("regionId", String.class, true, "ID of the region entered")
            .addDataField("regionName", String.class, true, "Name of the region entered")
            .addDataField("world", String.class, true, "World where the region is located");
        regionEnterEvent.addTag("region");
        regionEnterEvent.addTag("movement");
        registerEvent(regionEnterEvent);
        
        CustomEvent regionExitEvent = new CustomEvent("regionExit", "system")
            .addDataField("player", Player.class, true, "Player who exited the region")
            .addDataField("regionId", String.class, true, "ID of the region exited")
            .addDataField("regionName", String.class, true, "Name of the region exited")
            .addDataField("world", String.class, true, "World where the region is located");
        regionExitEvent.addTag("region");
        regionExitEvent.addTag("movement");
        registerEvent(regionExitEvent);
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
        
        
        regionDetectionSystem.updatePlayerRegions(event.getPlayer());
    }
    
    @org.bukkit.event.EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        data.put("player", DataValue.fromObject(event.getPlayer()));
        data.put("reason", DataValue.fromObject(event.getQuitMessage()));
        
        triggerEvent("playerDisconnect", data, event.getPlayer(), event.getPlayer().getWorld().getName());
        
        
        regionDetectionSystem.cleanupPlayerTracking(event.getPlayer().getUniqueId());
    }
    
    @org.bukkit.event.EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only trigger if player actually moved to a different block
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            
            // Trigger the custom playerMove event
            Map<String, DataValue> data = new HashMap<>();
            data.put("player", DataValue.fromObject(event.getPlayer()));
            data.put("from", DataValue.fromObject(event.getFrom()));
            data.put("to", DataValue.fromObject(event.getTo()));
            
            triggerEvent("playerMove", data, event.getPlayer(), event.getPlayer().getWorld().getName());
            
            // Update region tracking
            regionDetectionSystem.updatePlayerRegions(event.getPlayer());
        }
    }
    
    @org.bukkit.event.EventHandler
    public void onPlayerChat(org.bukkit.event.player.PlayerChatEvent event) {
        // Trigger the custom playerChat event
        Map<String, DataValue> data = new HashMap<>();
        data.put("player", DataValue.fromObject(event.getPlayer()));
        data.put("message", DataValue.fromObject(event.getMessage()));
        
        triggerEvent("playerChat", data, event.getPlayer(), event.getPlayer().getWorld().getName());
    }
    
    @org.bukkit.event.EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        // Trigger the custom playerDeath event
        Map<String, DataValue> data = new HashMap<>();
        data.put("player", DataValue.fromObject(event.getEntity()));
        if (event.getEntity().getKiller() != null) {
            data.put("killer", DataValue.fromObject(event.getEntity().getKiller()));
        }
        data.put("cause", DataValue.fromObject(event.getDeathMessage()));
        
        triggerEvent("playerDeath", data, event.getEntity(), event.getEntity().getWorld().getName());
    }
    
    @org.bukkit.event.EventHandler
    public void onPlayerRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        // Trigger the custom playerRespawn event
        Map<String, DataValue> data = new HashMap<>();
        data.put("player", DataValue.fromObject(event.getPlayer()));
        data.put("location", DataValue.fromObject(event.getRespawnLocation()));
        
        triggerEvent("playerRespawn", data, event.getPlayer(), event.getPlayer().getWorld().getName());
    }
    
    @org.bukkit.event.EventHandler
    public void onPlayerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        // Trigger the custom playerTeleport event
        Map<String, DataValue> data = new HashMap<>();
        data.put("player", DataValue.fromObject(event.getPlayer()));
        data.put("from", DataValue.fromObject(event.getFrom()));
        data.put("to", DataValue.fromObject(event.getTo()));
        
        triggerEvent("playerTeleport", data, event.getPlayer(), event.getPlayer().getWorld().getName());
    }
    
    @org.bukkit.event.EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        // Only handle player damage events
        if (event.getEntity() instanceof Player) {
            // Trigger the custom entityDamage event
            Map<String, DataValue> data = new HashMap<>();
            data.put("player", DataValue.fromObject((Player) event.getEntity()));
            data.put("damaged", DataValue.fromObject(event.getEntity()));
            data.put("damage", DataValue.fromObject(event.getDamage()));
            
            triggerEvent("entityDamage", data, (Player) event.getEntity(), event.getEntity().getWorld().getName());
        }
    }
    
    @org.bukkit.event.EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        // Only handle player inventory clicks
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            
            // Trigger the custom inventoryClick event
            Map<String, DataValue> data = new HashMap<>();
            data.put("player", DataValue.fromObject(player));
            data.put("slot", DataValue.fromObject(event.getSlot()));
            if (event.getCurrentItem() != null) {
                data.put("item", DataValue.fromObject(event.getCurrentItem()));
            }
            
            triggerEvent("inventoryClick", data, player, player.getWorld().getName());
        }
    }
    
    @org.bukkit.event.EventHandler
    public void onEntityPickupItem(org.bukkit.event.entity.EntityPickupItemEvent event) {
        // Only handle player pickup events
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            // Trigger the custom entityPickupItem event
            Map<String, DataValue> data = new HashMap<>();
            data.put("player", DataValue.fromObject(player));
            data.put("item", DataValue.fromObject(event.getItem().getItemStack()));
            
            triggerEvent("entityPickupItem", data, player, player.getWorld().getName());
        }
    }
    
    // Add a repeating task to trigger the tick event
    private void startTickTask() {
        // Run every 5 ticks instead of every tick to reduce server load
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            try {
                Map<String, DataValue> data = new HashMap<>();
                data.put("tickCount", DataValue.fromObject(System.currentTimeMillis()));
                
                // Only trigger if there are handlers registered for the tick event
                if (isEventRegistered("tick") && getEventHandlerCount("tick") > 0) {
                    triggerEvent("tick", data, null, "global");
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Error in tick task", e);
            }
        }, 5L, 5L); // Run every 5 ticks instead of every tick
    }
    
    /**
     * Gets the event correlation engine
     * 
     * @return the correlation engine
     */
    public EventCorrelationEngine getCorrelationEngine() {
        return correlationEngine;
    }
    
    /**
     * Gets the region detection system
     * 
     * @return the region detection system
     */
    public RegionDetectionSystem getRegionDetectionSystem() {
        return regionDetectionSystem;
    }
    
    /**
     * Starts the correlation cleanup task
     */
    private void startCorrelationCleanupTask() {
        
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            correlationEngine.cleanupExpiredInstances();
        }, 1200L, 1200L); 
    }
    
    /**
     * Gets an event by name, checking aliases and inheritance
     * 
     * @param eventName the name of the event to find
     * @return the event, or null if not found
     */
    public CustomEvent getEventByName(String eventName) {
        if (eventName == null) {
            return null;
        }
        
        
        CustomEvent event = eventDefinitions.get(eventName);
        if (event != null) {
            return event;
        }
        
        
        for (CustomEvent customEvent : eventDefinitions.values()) {
            if (customEvent != null && customEvent.getAliases().contains(eventName)) {
                return customEvent;
            }
        }
        
        return null;
    }
    
    /**
     * Gets events by tag
     * 
     * @param tag the tag to search for
     * @return a list of events with the tag
     */
    public List<CustomEvent> getEventsByTag(String tag) {
        if (tag == null) {
            return Collections.emptyList();
        }
        
        return eventDefinitions.values().stream()
            .filter(Objects::nonNull)
            .filter(event -> event.hasTag(tag))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets events by metadata
     * 
     * @param key The metadata key to search for
     * @param value The expected metadata value
     * @return List of events with matching metadata
     */
    public List<CustomEvent> getEventsByMetadata(String key, Object value) {
        if (key == null || value == null) {
            return Collections.emptyList();
        }
        
        return eventDefinitions.values().stream()
            .filter(Objects::nonNull)
            .filter(event -> {
                Object metadataValue = event.getMetadata(key, Object.class);
                return metadataValue != null && metadataValue.equals(value);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Registers an advanced event trigger
     * 
     * @param trigger the trigger to register
     */
    public void registerAdvancedTrigger(AdvancedEventTrigger trigger) {
        if (trigger != null) {
            advancedTriggers.put(trigger.getTriggerId(), trigger);
            log.info("Registered advanced event trigger: " + trigger.getTriggerId() + " for event: " + trigger.getEventName());
        }
    }
    
    /**
     * Unregisters an advanced event trigger
     * 
     * @param triggerId the ID of the trigger to unregister
     */
    public void unregisterAdvancedTrigger(String triggerId) {
        if (triggerId != null) {
            AdvancedEventTrigger trigger = advancedTriggers.remove(triggerId);
            if (trigger != null) {
                log.info("Unregistered advanced event trigger: " + triggerId);
            }
        }
    }
    
    /**
     * Gets an advanced event trigger by ID
     * 
     * @param triggerId the ID of the trigger to get
     * @return the trigger, or null if not found
     */
    public AdvancedEventTrigger getAdvancedTrigger(String triggerId) {
        return triggerId != null ? advancedTriggers.get(triggerId) : null;
    }
    
    /**
     * Gets all advanced triggers for an event
     * 
     * @param eventName the name of the event
     * @return a list of advanced triggers for the event
     */
    public List<AdvancedEventTrigger> getAdvancedTriggersForEvent(String eventName) {
        if (eventName == null) {
            return Collections.emptyList();
        }
        
        return advancedTriggers.values().stream()
            .filter(Objects::nonNull)
            .filter(trigger -> eventName.equals(trigger.getEventName()))
            .collect(Collectors.toList());
    }
    
    /**
     * Schedules an event trigger
     * 
     * @param triggerId the ID of the trigger
     * @param delayMs the delay in milliseconds
     * @param player the player associated with the trigger
     * @param world the world name
     */
    public void scheduleTrigger(String triggerId, long delayMs, Player player, String world) {
        if (triggerId != null) {
            ScheduledTrigger scheduled = new ScheduledTrigger(triggerId, delayMs, player, world, plugin);
            scheduledTriggers.put(triggerId, scheduled);
            scheduled.schedule(this);
        }
    }
    
    /**
     * Cancels a scheduled trigger
     * 
     * @param triggerId the ID of the trigger to cancel
     */
    public void cancelScheduledTrigger(String triggerId) {
        if (triggerId != null) {
            ScheduledTrigger scheduled = scheduledTriggers.remove(triggerId);
            if (scheduled != null) {
                scheduled.cancel();
            }
        }
    }
    
    /**
     * Executes an advanced trigger
     * 
     * @param triggerId the ID of the trigger to execute
     * @param player the player associated with the trigger
     * @param world the world name
     */
    public void executeAdvancedTrigger(String triggerId, Player player, String world) {
        if (triggerId != null) {
            AdvancedEventTrigger trigger = advancedTriggers.get(triggerId);
            if (trigger != null) {
                trigger.execute(this, player, world);
            }
        }
    }
    
    /**
     * Publishes an event to the event system.
     * 
     * @param event The event to publish
     */
    @Override
    public void publishEvent(CustomEvent event) {
        if (event != null) {
            
            Map<String, DataValue> eventData = new HashMap<>();
            
            eventData.put("event_name", DataValue.fromObject(event.getName()));
            eventData.put("event_category", DataValue.fromObject(event.getCategory()));
            
            eventDispatcher.dispatchEvent(event.getName(), eventData, null, null);
        }
    }
    
    /**
     * Publishes an event with associated data to the event system.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     */
    @Override
    public void publishEvent(String eventName, Map<String, DataValue> eventData) {
        eventDispatcher.dispatchEvent(eventName, eventData, null, null);
    }
    
    /**
     * Handles an event that has been published.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     * @param source The player that triggered the event (can be null)
     * @param worldName The world where the event occurred
     */
    @Override
    public void handleEvent(String eventName, Map<String, DataValue> eventData, Player source, String worldName) {
        
        triggerEvent(eventName, eventData, source, worldName);
    }
    
    /**
     * Gets the event names this subscriber is interested in.
     * 
     * @return An array of event names to subscribe to
     */
    @Override
    public String[] getSubscribedEvents() {
        
        return eventDefinitions.keySet().toArray(new String[0]);
    }
    
    /**
     * Gets the priority of this subscriber for event handling.
     * Higher priority subscribers are called first.
     * 
     * @return The priority level (higher numbers = higher priority)
     */
    @Override
    public int getPriority() {
        return 100; 
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
        private final MegaCreative plugin;
        
        public EventHandler(CodeBlock handlerBlock, Player player, String worldName, int priority, MegaCreative plugin) {
            this.handlerBlock = handlerBlock;
            this.playerId = player != null ? player.getUniqueId() : null;
            this.worldName = worldName;
            this.priority = priority;
            this.isGlobal = worldName == null;
            this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        }
        
        public boolean canHandle(Player source, String sourceWorld, Map<String, DataValue> eventData) {
            
            if (isGlobal) return true;
            
            
            if (worldName != null && !worldName.equals(sourceWorld)) return false;
            
            
            if (playerId != null && source != null && !playerId.equals(source.getUniqueId())) return false;
            
            return true;
        }
        
        public void handle(Map<String, DataValue> eventData, Player source, String sourceWorld) {
            
            
            if (handlerBlock == null) {
                return;
            }
            
            try {
                
                com.megacreative.MegaCreative plugin = this.plugin;
                if (plugin == null) {
                    log.warning("Plugin instance is null in EventHandler");
                    return;
                }
                
                
                com.megacreative.coding.variables.VariableManager variableManager = 
                    plugin.getServiceRegistry() != null ? plugin.getServiceRegistry().getVariableManager() : null;
                
                
                if (source != null && variableManager != null) {
                    for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                        try {
                            variableManager.setPlayerVariable(source.getUniqueId(), entry.getKey(), entry.getValue());
                        } catch (Exception e) {
                            log.log(Level.WARNING, "Failed to set player variable: " + entry.getKey(), e);
                        }
                    }
                }
                
                
                com.megacreative.coding.ScriptEngine scriptEngine = 
                    plugin.getServiceRegistry() != null ? 
                    plugin.getServiceRegistry().getService(com.megacreative.coding.ScriptEngine.class) : null;
                
                if (scriptEngine != null) {
                    
                    scriptEngine.executeBlock(handlerBlock, source, "event_handler")
                        .thenAccept(result -> {
                            if (result != null && !result.isSuccess()) {
                                log.warning("EventHandler execution failed: " + result.getMessage());
                            }
                        })
                        .exceptionally(throwable -> {
                            if (throwable != null) {
                                log.warning("Error in EventHandler: " + throwable.getMessage());
                            }
                            return null;
                        });
                } else {
                    
                    log.info("EventHandler called for player " + (source != null ? source.getName() : "unknown") + 
                            " in world " + sourceWorld);
                }
            } catch (Exception e) {
                
                log.log(Level.WARNING, "Error in EventHandler.handle()", e);
            }
        }
        
        
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
            this.eventName = Objects.requireNonNull(eventName, "Event name cannot be null");
            this.eventData = eventData != null ? new HashMap<>(eventData) : Collections.emptyMap();
            this.sourcePlayerId = source != null ? source.getUniqueId() : null;
            this.worldName = worldName;
            this.triggeredTime = System.currentTimeMillis();
        }
        
        
        public String getEventName() { return eventName; }
        public Map<String, DataValue> getEventData() { return eventData; }
        public UUID getSourcePlayerId() { return sourcePlayerId; }
        public String getWorldName() { return worldName; }
        public long getTriggeredTime() { return triggeredTime; }
        public int getHandlersExecuted() { return handlersExecuted; }
        public long getExecutionTime() { return executionTime; }
        
        
        public void setHandlersExecuted(int handlersExecuted) { this.handlersExecuted = handlersExecuted; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    }
    
    /**
     * Scheduled trigger wrapper
     */
    private static class ScheduledTrigger {
        private final String triggerId;
        private final long delayMs;
        private final UUID playerId;
        private final String worldName;
        private final MegaCreative plugin; 
        private boolean cancelled = false;
        private int taskId = -1; 
        
        public ScheduledTrigger(String triggerId, long delayMs, Player player, String worldName, MegaCreative plugin) {
            this.triggerId = Objects.requireNonNull(triggerId, "Trigger ID cannot be null");
            this.delayMs = delayMs;
            this.playerId = player != null ? player.getUniqueId() : null;
            this.worldName = worldName;
            this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null"); 
        }
        
        public void schedule(CustomEventManager manager) {
            if (manager == null) {
                log.warning("Cannot schedule trigger: manager is null");
                return;
            }
            
            
            this.taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (!cancelled) {
                    try {
                        manager.executeAdvancedTrigger(triggerId, 
                            playerId != null ? plugin.getServer().getPlayer(playerId) : null, 
                            worldName);
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Error executing scheduled trigger: " + triggerId, e);
                    }
                }
            }, delayMs / 50); 
        }
        
        public void cancel() {
            cancelled = true;
            if (taskId != -1 && plugin != null && plugin.getServer() != null) {
                try {
                    plugin.getServer().getScheduler().cancelTask(taskId);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error cancelling scheduled task: " + taskId, e);
                }
            }
        }
    }
}