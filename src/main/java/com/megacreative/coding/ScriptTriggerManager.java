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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

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
        customEventManager.registerEventHandler("inventoryOpen", createPlayerEventHandler("onInventoryOpen"));
        customEventManager.registerEventHandler("entityPickupItem", createPlayerEventHandler("onEntityPickupItem"));
        
        // Register handlers for global events
        customEventManager.registerEventHandler("tick", createGlobalEventHandler("onTick"));
        customEventManager.registerEventHandler("second", createGlobalEventHandler("onSecond"));
        customEventManager.registerEventHandler("minute", createGlobalEventHandler("onMinute"));
        
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
                return;
            }
            
            // Create a game event with context data
            GameEvent gameEvent = new GameEvent(eventName);
            if (player != null) {
                gameEvent.setPlayer(player);
            }
            
            // Find all event handlers for this event type
            Map<UUID, CustomEventManager.EventHandler> handlers = customEventManager.getEventHandlers(eventName);
            if (handlers != null && !handlers.isEmpty()) {
                for (CustomEventManager.EventHandler handler : handlers.values()) {
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
                return;
            }
            
            // Create a game event with context data
            GameEvent gameEvent = new GameEvent(eventName);
            
            // Find all event handlers for this event type
            Map<UUID, CustomEventManager.EventHandler> handlers = customEventManager.getEventHandlers(eventName);
            if (handlers != null && !handlers.isEmpty()) {
                for (CustomEventManager.EventHandler handler : handlers.values()) {
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
    
    // Deprecated methods - kept for backward compatibility but no longer used
    @Deprecated
    @EventHandler
    public void onMegaPlayerJoin(MegaPlayerJoinedEvent event) {
        // This method is deprecated and no longer used
        // All event handling is now done through CustomEventManager
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerMove(MegaPlayerMoveEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerChat(MegaPlayerChatEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaBlockPlace(MegaBlockPlaceEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaBlockBreak(MegaBlockBreakEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaEntityPickupItem(MegaEntityPickupItemEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerDeath(MegaPlayerDeathEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerQuit(MegaPlayerQuitEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerRespawn(MegaPlayerRespawnEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaPlayerTeleport(MegaPlayerTeleportEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaEntityDamage(MegaEntityDamageEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaInventoryClick(MegaInventoryClickEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onMegaInventoryOpen(MegaInventoryOpenEvent event) {
        // This method is deprecated and no longer used
    }
    
    @Deprecated
    @EventHandler
    public void onTick(com.megacreative.events.TickEvent event) {
        // This method is deprecated and no longer used
    }
}