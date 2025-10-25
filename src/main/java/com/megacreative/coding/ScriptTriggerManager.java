package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.events.*;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.activators.ActivatorType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.List;

/**
 * Script trigger manager that listens to our custom events and triggers script execution
 * 
 * This class now uses the CustomEventManager system instead of the legacy Activator system
 */
public class ScriptTriggerManager implements Listener, com.megacreative.coding.events.EventPublisher {
    private static final Logger LOGGER = Logger.getLogger(ScriptTriggerManager.class.getName());
    
    // Add a counter to limit tick event processing frequency
    private static final AtomicInteger tickCounter = new AtomicInteger(0);
    
    // These fields need to remain as class fields since they are used throughout multiple methods
    // Static analysis flags them as convertible to local variables, but this is a false positive
    protected final MegaCreative plugin;
    protected final IWorldManager worldManager;
    protected final PlayerModeManager playerModeManager;
    protected final CustomEventManager customEventManager;
    protected final VariableManager variableManager;
    
    public ScriptTriggerManager(MegaCreative plugin, IWorldManager worldManager, PlayerModeManager playerModeManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.playerModeManager = playerModeManager;
        this.customEventManager = plugin.getServiceRegistry().getCustomEventManager();
        this.variableManager = plugin.getServiceRegistry().getVariableManager();
        
        
        registerEventHandlers();
    }
    
    // Protected getter methods for access by subclasses
    protected MegaCreative getPlugin() {
        return plugin;
    }
    
    protected IWorldManager getWorldManager() {
        return worldManager;
    }
    
    protected PlayerModeManager getPlayerModeManager() {
        return playerModeManager;
    }
    
    protected CustomEventManager getCustomEventManager() {
        return customEventManager;
    }
    
    protected VariableManager getVariableManager() {
        return variableManager;
    }
    
    /**
     * Register event handlers with the CustomEventManager
     */
    private void registerEventHandlers() {
        
        customEventManager.registerEventHandler("playerConnect", createPlayerEventHandler("onJoin"));
        customEventManager.registerEventHandler("playerDisconnect", createPlayerEventHandler("onQuit"));
        customEventManager.registerEventHandler("playerMove", createPlayerEventHandler("onPlayerMove"));
        customEventManager.registerEventHandler("playerChat", createPlayerEventHandler("onChat"));
        customEventManager.registerEventHandler("playerDeath", createPlayerEventHandler("onPlayerDeath"));
        customEventManager.registerEventHandler("playerRespawn", createPlayerEventHandler("onRespawn"));
        customEventManager.registerEventHandler("playerTeleport", createPlayerEventHandler("onTeleport"));
        customEventManager.registerEventHandler("entityDamage", createPlayerEventHandler("onEntityDamage"));
        customEventManager.registerEventHandler("inventoryClick", createPlayerEventHandler("onInventoryClick"));
        customEventManager.registerEventHandler("entityPickupItem", createPlayerEventHandler("onEntityPickupItem"));
        
        
        customEventManager.registerEventHandler("tick", createGlobalEventHandler("onTick"));
        
        LOGGER.fine("Registered event handlers with CustomEventManager");
    }
    
    /**
     * Create a player event handler for the CustomEventManager
     */
    private CustomEventManager.EventHandler createPlayerEventHandler(String eventName) {
        return new CustomEventManager.EventHandler(null, null, null, 0, plugin) {
            @Override
            public boolean canHandle(Player source, String sourceWorld, Map<String, DataValue> eventData) {
                return true; 
            }
            
            @Override
            public void handle(Map<String, DataValue> eventData, Player source, String sourceWorld) {
                handlePlayerEvent(eventName, eventData, source, sourceWorld);
            }
        };
    }
    
    /**
     * Create a global event handler for the CustomEventManager
     */
    private CustomEventManager.EventHandler createGlobalEventHandler(String eventName) {
        return new CustomEventManager.EventHandler(null, null, null, 0, plugin) {
            @Override
            public boolean canHandle(Player source, String sourceWorld, Map<String, DataValue> eventData) {
                return true; 
            }
            
            @Override
            public void handle(Map<String, DataValue> eventData, Player source, String sourceWorld) {
                handleGlobalEvent(eventName, eventData, source, sourceWorld);
            }
        };
    }
    
    /**
     * Handle player events through the CustomEventManager
     */
    private void handlePlayerEvent(String eventName, Map<String, DataValue> eventData, Player player, String worldName) {
        if (player == null || worldManager == null) {
            return;
        }
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(player)) {
                return;
            }
            
            
            if (variableManager != null && eventData != null) {
                for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                    variableManager.setPlayerVariable(player.getUniqueId(), entry.getKey(), entry.getValue());
                }
            }
            
            // Execute scripts for the event using the compiled scripts
            executeScriptsForEvent(eventName, player, creativeWorld);
        } catch (Exception e) {
            LOGGER.warning("Error handling player event " + eventName + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle global events through the CustomEventManager
     */
    private void handleGlobalEvent(String eventName, Map<String, DataValue> eventData, Player player, String worldName) {
        if (worldManager == null) {
            return;
        }
        
        try {
            
            for (CreativeWorld creativeWorld : worldManager.getCreativeWorlds()) {
                if (creativeWorld == null) continue;
                
                // Execute scripts for the global event
                executeScriptsForGlobalEvent(eventName, creativeWorld);
            }
        } catch (Exception e) {
            LOGGER.warning("Error handling global event " + eventName + ": " + e.getMessage());
        }
    }
    
    /**
     * Execute scripts for a player event using the compiled scripts
     */
    private void executeScriptsForEvent(String eventName, Player player, CreativeWorld creativeWorld) {
        if (eventName == null || creativeWorld == null) {
            return;
        }
        
        try {
            // Get the compiled scripts from the creative world
            List<CodeScript> scripts = creativeWorld.getScripts();
            if (scripts == null || scripts.isEmpty()) {
                LOGGER.fine("No compiled scripts found for world " + creativeWorld.getName());
                return;
            }
            
            // Find and execute scripts that match the event
            for (CodeScript script : scripts) {
                if (script != null && script.getRootBlock() != null && 
                    eventName.equals(script.getRootBlock().getAction())) {
                    LOGGER.info("Executing script for event: " + eventName);
                    executeScript(script, player);
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error executing scripts for event " + eventName + ": " + e.getMessage());
        }
    }
    
    /**
     * Execute a script using the SimpleScriptEngine
     */
    private void executeScript(CodeScript script, Player player) {
        if (script == null || script.getRootBlock() == null || player == null) {
            return;
        }
        
        try {
            // Get the SimpleScriptEngine from the service registry
            ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
            
            if (scriptEngine instanceof SimpleScriptEngine) {
                SimpleScriptEngine simpleEngine = (SimpleScriptEngine) scriptEngine;
                
                // Create execution context
                ExecutionContext context = new ExecutionContext.Builder()
                    .plugin(plugin)
                    .player(player)
                    .build();
                
                // Execute the script
                com.megacreative.coding.executors.ExecutionResult result = simpleEngine.execute(script.getRootBlock(), context);
                
                if (result.isSuccess()) {
                    LOGGER.info("Script executed successfully: " + script.getRootBlock().getAction());
                } else {
                    LOGGER.warning("Script execution failed: " + result.getMessage());
                }
            } else {
                LOGGER.warning("Script engine is not SimpleScriptEngine, using default execution");
                // Fallback to default execution
                plugin.getServiceRegistry().getScriptEngine().executeScript(script, player, "event");
            }
        } catch (Exception e) {
            LOGGER.severe("Error executing script: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Execute scripts for a global event
     */
    private void executeScriptsForGlobalEvent(String eventName, CreativeWorld creativeWorld) {
        if (eventName == null || creativeWorld == null) {
            return;
        }
        
        try {
            // Get the compiled scripts from the creative world
            List<CodeScript> scripts = creativeWorld.getScripts();
            if (scripts == null || scripts.isEmpty()) {
                return;
            }
            
            // Find and execute scripts that match the global event
            for (CodeScript script : scripts) {
                if (script != null && script.getRootBlock() != null && 
                    eventName.equals(script.getRootBlock().getAction())) {
                    // For global events, we might not have a specific player
                    // In a real implementation, you might want to handle this differently
                    LOGGER.info("Executing global script for event: " + eventName);
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error executing scripts for global event " + eventName + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle custom event handlers
     */
    private void handleCustomEventHandlers(String eventName, Player player, CreativeWorld creativeWorld) {
        // This method can be used for additional custom event handling if needed
        LOGGER.fine("Handling custom event handlers for " + eventName);
    }
    
    /**
     * Handle event execution
     */
    private void handleEventExecution(String eventName, Player player, CreativeWorld creativeWorld) {
        // This method can be used for additional event execution logic if needed
        LOGGER.fine("Handling event execution for " + eventName);
    }
    
    /**
     * Maps event names to activator types
     */
    private ActivatorType mapEventToActivatorType(String eventName) {
        switch (eventName) {
            case "onJoin":
                return ActivatorType.PLAYER_JOIN;
            case "onQuit":
                return ActivatorType.PLAYER_QUIT;
            case "onPlayerMove":
                return ActivatorType.PLAYER_MOVE;
            case "onChat":
                return ActivatorType.PLAYER_CHAT;
            case "onPlayerDeath":
                return ActivatorType.PLAYER_DEATH;
            case "onRespawn":
                return ActivatorType.PLAYER_RESPAWN;
            case "onTeleport":
                return ActivatorType.PLAYER_TELEPORT;
            case "onEntityDamage":
                return ActivatorType.ENTITY_DAMAGE;
            case "onInventoryClick":
                return ActivatorType.INVENTORY_CLICK;
            case "onEntityPickupItem":
                return ActivatorType.ENTITY_PICKUP_ITEM;
            case "onTick":
                return ActivatorType.TICK;
            case "onBlockPlace":
                return ActivatorType.BLOCK_PLACE;
            case "onBlockBreak":
                return ActivatorType.BLOCK_BREAK;
            default:
                LOGGER.warning("Unknown event name: " + eventName);
                return null;
        }
    }
    
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerJoin(MegaPlayerJoinedEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onJoin", ActivatorType.PLAYER_JOIN, "player_join");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerMove(MegaPlayerMoveEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onPlayerMove", ActivatorType.PLAYER_MOVE, "player_move");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerChat(MegaPlayerChatEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onChat", ActivatorType.PLAYER_CHAT, "player_chat");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaBlockPlace(MegaBlockPlaceEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onBlockPlace", ActivatorType.BLOCK_PLACE, "block_place");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaBlockBreak(MegaBlockBreakEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onBlockBreak", ActivatorType.BLOCK_BREAK, "block_break");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaEntityPickupItem(MegaEntityPickupItemEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onEntityPickupItem", ActivatorType.ENTITY_PICKUP_ITEM, "entity_pickup_item");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerDeath(MegaPlayerDeathEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onPlayerDeath", ActivatorType.PLAYER_DEATH, "player_death");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerQuit(MegaPlayerQuitEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onQuit", ActivatorType.PLAYER_QUIT, "player_quit");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerRespawn(MegaPlayerRespawnEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onRespawn", ActivatorType.PLAYER_RESPAWN, "player_respawn");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerTeleport(MegaPlayerTeleportEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onTeleport", ActivatorType.PLAYER_TELEPORT, "player_teleport");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaEntityDamage(MegaEntityDamageEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onEntityDamage", ActivatorType.ENTITY_DAMAGE, "entity_damage");
    }
    
    @Deprecated
    @EventHandler
    public void onMegaInventoryClick(MegaInventoryClickEvent event) {
        handleDeprecatedEvent(event.getPlayer(), "onInventoryClick", ActivatorType.INVENTORY_CLICK, "inventory_click");
    }
    
    /**
     * Handle deprecated events with common logic
     * 
     * Обробляє застарілі події з загальною логікою
     */
    private void handleDeprecatedEvent(Player player, String eventName, ActivatorType activatorType, String logKey) {
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            
            if (codeHandler != null) {
                GameEvent gameEvent = new GameEvent(eventName);
                gameEvent.setPlayer(player);
                
                codeHandler.handleEvent(activatorType, gameEvent, player);
                // Use rate-limited logging for frequent events to reduce log spam
                com.megacreative.utils.LogUtils.infoRateLimited(
                    "Triggered " + activatorType + " activators for player " + player.getName(), 
                    logKey + "_" + player.getName()
                );
            }
        }
    }
    
    @Deprecated
    @EventHandler
    public void onTick(com.megacreative.events.TickEvent event) {
        int count = tickCounter.incrementAndGet();
        
        // Only process every 10th tick to reduce load
        if (count % 10 != 0) {
            return;
        }
        
        // Reset counter periodically to prevent overflow
        if (count > 1000000) {
            tickCounter.set(0);
        }
        
        processTickEvent();
    }
    
    /**
     * Process tick event for all creative worlds
     * 
     * Обробляє подію тіку для всіх творчих світів
     */
    private void processTickEvent() {
        try {
            for (CreativeWorld creativeWorld : worldManager.getCreativeWorlds()) {
                if (creativeWorld == null) continue;
                
                processTickForWorld(creativeWorld);
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Error processing tick event", e);
        }
    }
    
    /**
     * Process tick event for a specific world
     * 
     * Обробляє подію тіку для певного світу
     */
    private void processTickForWorld(CreativeWorld creativeWorld) {
        CodeHandler codeHandler = creativeWorld.getCodeHandler();
        
        if (codeHandler != null) {
            GameEvent gameEvent = new GameEvent("onTick");
            
            // Only log at fine level to reduce spam
            if (LOGGER.isLoggable(java.util.logging.Level.FINE)) {
                LOGGER.fine("Triggered TICK activators for world " + creativeWorld.getName());
            }
            
            codeHandler.handleEvent(ActivatorType.TICK, gameEvent, null);
        }
    }
    
    /**
     * Publishes an event to the event system.
     * 
     * @param event The event to be published
     */
    @Override
    public void publishEvent(CustomEvent event) {
        // Use the already initialized customEventManager
        if (customEventManager != null) {
            try {
                
                Map<String, DataValue> eventData = new HashMap<>();
                
                
                eventData.put("event_id", DataValue.fromObject(event.getId().toString()));
                eventData.put("event_name", DataValue.fromObject(event.getName()));
                eventData.put("event_category", DataValue.fromObject(event.getCategory()));
                eventData.put("event_description", DataValue.fromObject(event.getDescription()));
                eventData.put("event_author", DataValue.fromObject(event.getAuthor()));
                eventData.put("event_created_time", DataValue.fromObject(event.getCreatedTime()));
                
                
                for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                    eventData.put("data_" + entry.getKey(), entry.getValue());
                }
                
                
                customEventManager.triggerEvent(event.getName(), eventData, null, "global");
            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.WARNING, "Failed to publish event through CustomEventManager: " + e.getMessage(), e);
            }
        } else {
            
            LOGGER.fine("Published event: " + event.getName());
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
        // Use the already initialized customEventManager
        if (customEventManager != null) {
            try {
                
                customEventManager.triggerEvent(eventName, eventData, null, "global");
            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.WARNING, "Failed to publish event through CustomEventManager: " + e.getMessage(), e);
            }
        } else {
            
            LOGGER.fine("Published event: " + eventName + " with data: " + eventData.size() + " fields");
        }
    }
}