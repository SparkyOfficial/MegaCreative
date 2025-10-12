package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.events.*;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.events.CustomEventManager;
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
public class ScriptTriggerManager implements Listener {
    private static final Logger LOGGER = Logger.getLogger(ScriptTriggerManager.class.getName());
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final PlayerModeManager playerModeManager;
    private final CustomEventManager customEventManager;
    private final VariableManager variableManager;
    
    public ScriptTriggerManager(MegaCreative plugin, IWorldManager worldManager, PlayerModeManager playerModeManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.playerModeManager = playerModeManager;
        this.customEventManager = plugin.getServiceRegistry().getCustomEventManager();
        this.variableManager = plugin.getServiceRegistry().getVariableManager();
        
        // Register event handlers with the CustomEventManager
        registerEventHandlers();
    }
    
    /**
     * Register event handlers with the CustomEventManager
     */
    private void registerEventHandlers() {
        // Register handlers for player events
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
        
        // Register handlers for global events
        customEventManager.registerEventHandler("tick", createGlobalEventHandler("onTick"));
        
        LOGGER.info("Registered event handlers with CustomEventManager");
    }
    
    /**
     * Create a player event handler for the CustomEventManager
     */
    private CustomEventManager.EventHandler createPlayerEventHandler(String eventName) {
        return new CustomEventManager.EventHandler(null, null, null, 0, plugin) {
            @Override
            public boolean canHandle(Player source, String sourceWorld, Map<String, DataValue> eventData) {
                return true; // Handle all player events
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
                return true; // Handle all global events
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
            
            // Set event data as variables in the player's context
            if (variableManager != null && eventData != null) {
                for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                    variableManager.setPlayerVariable(player.getUniqueId(), entry.getKey(), entry.getValue());
                }
            }
            
            // Execute scripts for this event
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
            // For global events, we execute scripts in all loaded worlds
            for (CreativeWorld creativeWorld : worldManager.getCreativeWorlds()) {
                if (creativeWorld == null) continue;
                
                // Set event data as variables if we have a player
                if (player != null && variableManager != null && eventData != null) {
                    for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                        variableManager.setPlayerVariable(player.getUniqueId(), entry.getKey(), entry.getValue());
                    }
                }
                
                // Execute scripts for this global event
                executeScriptsForGlobalEvent(eventName, creativeWorld);
            }
        } catch (Exception e) {
            LOGGER.warning("Error handling global event " + eventName + ": " + e.getMessage());
        }
    }
    
    /**
     * Execute scripts for a player event
     */
    private void executeScriptsForEvent(String eventName, Player player, CreativeWorld creativeWorld) {
        if (eventName == null || creativeWorld == null) {
            return;
        }
        
        try {
            // Get the code handler for this world
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) {
                LOGGER.warning("CodeHandler is null for world: " + creativeWorld.getName());
                return;
            }
            
            // Create a game event with context data
            GameEvent gameEvent = new GameEvent(eventName);
            if (player != null) {
                gameEvent.setPlayer(player);
            }
            
            // Map event names to activator types
            ActivatorType activatorType = mapEventToActivatorType(eventName);
            if (activatorType != null) {
                LOGGER.info("Executing scripts for event: " + eventName + " with activator type: " + activatorType);
                codeHandler.handleEvent(activatorType, gameEvent, player);
            } else {
                LOGGER.warning("No activator type found for event: " + eventName);
            }
            
            // Find all event handlers for this event type
            List<CustomEventManager.EventHandler> handlers = customEventManager.getEventHandlers(eventName);
            if (handlers != null && !handlers.isEmpty()) {
                for (CustomEventManager.EventHandler handler : handlers) {
                    // Check if handler can process this event
                    if (handler.canHandle(player, creativeWorld.getName(), gameEvent.getEventData())) {
                        // Execute the handler with event data
                        handler.handle(gameEvent.getEventData(), player, creativeWorld.getName());
                        
                        // Log successful execution
                        if (LOGGER.isLoggable(java.util.logging.Level.FINE)) {
                            LOGGER.fine("Executed event handler for " + eventName + " in world " + creativeWorld.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error executing scripts for event " + eventName + ": " + e.getMessage());
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
            // Get the code handler for this world
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) {
                LOGGER.warning("CodeHandler is null for world: " + creativeWorld.getName());
                return;
            }
            
            // Create a game event with context data
            GameEvent gameEvent = new GameEvent(eventName);
            
            // Map event names to activator types
            ActivatorType activatorType = mapEventToActivatorType(eventName);
            if (activatorType != null) {
                LOGGER.info("Executing scripts for global event: " + eventName + " with activator type: " + activatorType);
                codeHandler.handleEvent(activatorType, gameEvent, null);
            } else {
                LOGGER.warning("No activator type found for global event: " + eventName);
            }
            
            // Find all event handlers for this event type
            List<CustomEventManager.EventHandler> handlers = customEventManager.getEventHandlers(eventName);
            if (handlers != null && !handlers.isEmpty()) {
                for (CustomEventManager.EventHandler handler : handlers) {
                    // Check if handler can process this event
                    if (handler.canHandle(null, creativeWorld.getName(), gameEvent.getEventData())) {
                        // Execute the handler with event data
                        handler.handle(gameEvent.getEventData(), null, creativeWorld.getName());
                        
                        // Log successful execution
                        if (LOGGER.isLoggable(java.util.logging.Level.FINE)) {
                            LOGGER.fine("Executed global event handler for " + eventName + " in world " + creativeWorld.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error executing scripts for global event " + eventName + ": " + e.getMessage());
            e.printStackTrace();
        }
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
    
    // Deprecated methods - kept for backward compatibility but no longer used
    @Deprecated
    @EventHandler
    public void onMegaPlayerJoin(MegaPlayerJoinedEvent event) {
        // This method is deprecated and no longer used
        // All event handling is now done through CustomEventManager
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onJoin");
            gameEvent.setPlayer(player);
            // Trigger all PLAYER_JOIN activators
            codeHandler.handleEvent(ActivatorType.PLAYER_JOIN, gameEvent, player);
            LOGGER.info("Triggered PLAYER_JOIN activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerMove(MegaPlayerMoveEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onPlayerMove");
            gameEvent.setPlayer(player);
            // Trigger all PLAYER_MOVE activators
            codeHandler.handleEvent(ActivatorType.PLAYER_MOVE, gameEvent, player);
            LOGGER.info("Triggered PLAYER_MOVE activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerChat(MegaPlayerChatEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onChat");
            gameEvent.setPlayer(player);
            // Trigger all PLAYER_CHAT activators
            codeHandler.handleEvent(ActivatorType.PLAYER_CHAT, gameEvent, player);
            LOGGER.info("Triggered PLAYER_CHAT activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaBlockPlace(MegaBlockPlaceEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onBlockPlace");
            gameEvent.setPlayer(player);
            // Trigger all BLOCK_PLACE activators
            codeHandler.handleEvent(ActivatorType.BLOCK_PLACE, gameEvent, player);
            LOGGER.info("Triggered BLOCK_PLACE activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaBlockBreak(MegaBlockBreakEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onBlockBreak");
            gameEvent.setPlayer(player);
            // Trigger all BLOCK_BREAK activators
            codeHandler.handleEvent(ActivatorType.BLOCK_BREAK, gameEvent, player);
            LOGGER.info("Triggered BLOCK_BREAK activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaEntityPickupItem(MegaEntityPickupItemEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onEntityPickupItem");
            gameEvent.setPlayer(player);
            // Trigger all ENTITY_PICKUP_ITEM activators
            codeHandler.handleEvent(ActivatorType.ENTITY_PICKUP_ITEM, gameEvent, player);
            LOGGER.info("Triggered ENTITY_PICKUP_ITEM activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerDeath(MegaPlayerDeathEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onPlayerDeath");
            gameEvent.setPlayer(player);
            // Trigger all PLAYER_DEATH activators
            codeHandler.handleEvent(ActivatorType.PLAYER_DEATH, gameEvent, player);
            LOGGER.info("Triggered PLAYER_DEATH activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerQuit(MegaPlayerQuitEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onQuit");
            gameEvent.setPlayer(player);
            // Trigger all PLAYER_QUIT activators
            codeHandler.handleEvent(ActivatorType.PLAYER_QUIT, gameEvent, player);
            LOGGER.info("Triggered PLAYER_QUIT activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerRespawn(MegaPlayerRespawnEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onRespawn");
            gameEvent.setPlayer(player);
            // Trigger all PLAYER_RESPAWN activators
            codeHandler.handleEvent(ActivatorType.PLAYER_RESPAWN, gameEvent, player);
            LOGGER.info("Triggered PLAYER_RESPAWN activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerTeleport(MegaPlayerTeleportEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onTeleport");
            gameEvent.setPlayer(player);
            // Trigger all PLAYER_TELEPORT activators
            codeHandler.handleEvent(ActivatorType.PLAYER_TELEPORT, gameEvent, player);
            LOGGER.info("Triggered PLAYER_TELEPORT activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaEntityDamage(MegaEntityDamageEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onEntityDamage");
            gameEvent.setPlayer(player);
            // Trigger all ENTITY_DAMAGE activators
            codeHandler.handleEvent(ActivatorType.ENTITY_DAMAGE, gameEvent, player);
            LOGGER.info("Triggered ENTITY_DAMAGE activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onMegaInventoryClick(MegaInventoryClickEvent event) {
        // This method is deprecated and no longer used
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());

        if (creativeWorld != null) {
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onInventoryClick");
            gameEvent.setPlayer(player);
            // Trigger all INVENTORY_CLICK activators
            codeHandler.handleEvent(ActivatorType.INVENTORY_CLICK, gameEvent, player);
            LOGGER.info("Triggered INVENTORY_CLICK activators for player " + player.getName());
        }
    }
    
    @Deprecated
    @EventHandler
    public void onTick(com.megacreative.events.TickEvent event) {
        // This method is deprecated and no longer used
        // For global events, we execute scripts in all loaded worlds
        for (CreativeWorld creativeWorld : worldManager.getCreativeWorlds()) {
            if (creativeWorld == null) continue;
            
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            // Create a game event
            GameEvent gameEvent = new GameEvent("onTick");
            // Trigger all TICK activators
            codeHandler.handleEvent(ActivatorType.TICK, gameEvent, null);
            LOGGER.info("Triggered TICK activators for world " + creativeWorld.getName());
        }
    }
}