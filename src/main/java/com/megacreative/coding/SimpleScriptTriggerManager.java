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
import java.util.logging.Logger;
import java.util.List;

/**
 * Simplified script trigger manager that listens to our custom events and triggers script execution
 * 
 * This class focuses on the core functionality:
 * 1. Listening to CustomEventManager events
 * 2. Finding all CodeBlock-ы типа EVENT that correspond to the event
 * 3. Calling scriptEngine.executeScript(eventBlock, ...) for each found block
 */
public class SimpleScriptTriggerManager extends ScriptTriggerManager {
    private static final Logger LOGGER = Logger.getLogger(SimpleScriptTriggerManager.class.getName());
    
    private final ScriptEngine scriptEngine;
    
    public SimpleScriptTriggerManager(MegaCreative plugin, IWorldManager worldManager, PlayerModeManager playerModeManager) {
        super(plugin, worldManager, playerModeManager);
        this.scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        
        // Override the event handler registration with our simplified version
        registerEventHandlers();
    }
    
    /**
     * Register event handlers with the CustomEventManager
     */
    private void registerEventHandlers() {
        // Register common player events
        getCustomEventManager().registerEventHandler("playerConnect", createPlayerEventHandler("onJoin"));
        getCustomEventManager().registerEventHandler("playerDisconnect", createPlayerEventHandler("onQuit"));
        getCustomEventManager().registerEventHandler("playerMove", createPlayerEventHandler("onPlayerMove"));
        getCustomEventManager().registerEventHandler("playerChat", createPlayerEventHandler("onChat"));
        getCustomEventManager().registerEventHandler("playerDeath", createPlayerEventHandler("onPlayerDeath"));
        getCustomEventManager().registerEventHandler("playerRespawn", createPlayerEventHandler("onRespawn"));
        getCustomEventManager().registerEventHandler("playerTeleport", createPlayerEventHandler("onTeleport"));
        getCustomEventManager().registerEventHandler("entityDamage", createPlayerEventHandler("onEntityDamage"));
        getCustomEventManager().registerEventHandler("inventoryClick", createPlayerEventHandler("onInventoryClick"));
        getCustomEventManager().registerEventHandler("entityPickupItem", createPlayerEventHandler("onEntityPickupItem"));
        
        // Register global events
        getCustomEventManager().registerEventHandler("tick", createGlobalEventHandler("onTick"));
        
        LOGGER.fine("Registered event handlers with CustomEventManager");
    }
    
    /**
     * Create a player event handler for the CustomEventManager
     */
    private CustomEventManager.EventHandler createPlayerEventHandler(String eventName) {
        return new CustomEventManager.EventHandler(null, null, null, 0, getPlugin()) {
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
        return new CustomEventManager.EventHandler(null, null, null, 0, getPlugin()) {
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
        if (player == null || getWorldManager() == null) {
            return;
        }
        
        try {
            CreativeWorld creativeWorld = getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(player)) {
                return;
            }
            
            // Set variables from event data
            if (getVariableManager() != null && eventData != null) {
                for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                    getVariableManager().setPlayerVariable(player.getUniqueId(), entry.getKey(), entry.getValue());
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
        if (getWorldManager() == null) {
            return;
        }
        
        try {
            // Execute scripts for this global event in all creative worlds
            for (CreativeWorld creativeWorld : getWorldManager().getCreativeWorlds()) {
                if (creativeWorld == null) continue;
                
                // Set variables from event data
                if (player != null && getVariableManager() != null && eventData != null) {
                    for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                        getVariableManager().setPlayerVariable(player.getUniqueId(), entry.getKey(), entry.getValue());
                    }
                }
                
                // Execute scripts for this global event only if it's not the tick event or if we have scripts
                if (!"tick".equals(eventName) || !creativeWorld.getScripts().isEmpty()) {
                    executeScriptsForGlobalEvent(eventName, creativeWorld);
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error handling global event " + eventName + ": " + e.getMessage());
        }
    }
    
    /**
     * Execute scripts for a player event
     */
    private void executeScriptsForEvent(String eventName, Player player, CreativeWorld creativeWorld) {
        if (eventName == null || creativeWorld == null || scriptEngine == null) {
            return;
        }
        
        try {
            // Find all event blocks that match this event name
            List<CodeBlock> eventBlocks = findEventBlocks(eventName, creativeWorld);
            
            // Execute each event block only if we found some
            if (!eventBlocks.isEmpty()) {
                // Add debug message only when we have blocks to execute
                if (player != null) {
                    player.sendMessage("§aExecuting " + eventBlocks.size() + " scripts for event: " + eventName);
                }
                getPlugin().getLogger().fine("SimpleScriptTriggerManager: Executing " + eventBlocks.size() + " scripts for event " + eventName + " for player " + (player != null ? player.getName() : "null"));
                
                for (CodeBlock eventBlock : eventBlocks) {
                    // Add debug logging
                    if (player != null) {
                        player.sendMessage("§aExecuting event block with action: " + eventBlock.getAction());
                        getPlugin().getLogger().fine("SimpleScriptTriggerManager: Executing event block with action " + eventBlock.getAction() + " for player " + player.getName());
                    }
                    
                    // Create a script from the event block
                    CodeScript script = new CodeScript(eventBlock);
                    
                    // Execute the script
                    scriptEngine.executeScript(script, player, eventName);
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error executing scripts for event " + eventName + ": " + e.getMessage());
            LOGGER.log(java.util.logging.Level.SEVERE, "Error executing scripts for event " + eventName, e);
            if (player != null) {
                player.sendMessage("§cError executing scripts: " + e.getMessage());
            }
        }
    }
    
    /**
     * Execute scripts for a global event
     */
    private void executeScriptsForGlobalEvent(String eventName, CreativeWorld creativeWorld) {
        if (eventName == null || creativeWorld == null || scriptEngine == null) {
            return;
        }
        
        try {
            // Find all event blocks that match this event name
            List<CodeBlock> eventBlocks = findEventBlocks(eventName, creativeWorld);
            
            // Execute each event block only if we found some
            if (!eventBlocks.isEmpty()) {
                getPlugin().getLogger().fine("SimpleScriptTriggerManager: Executing " + eventBlocks.size() + " global scripts for event " + eventName + " in world " + creativeWorld.getWorldName());
                
                // Execute each event block
                for (CodeBlock eventBlock : eventBlocks) {
                    // Create a script from the event block
                    CodeScript script = new CodeScript(eventBlock);
                    
                    // Execute the script (no player for global events)
                    scriptEngine.executeScript(script, null, eventName);
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error executing scripts for global event " + eventName + ": " + e.getMessage());
            LOGGER.log(java.util.logging.Level.SEVERE, "Error executing scripts for global event " + eventName, e);
        }
    }
    
    /**
     * Find all event blocks that match the given event name in a creative world
     */
    private List<CodeBlock> findEventBlocks(String eventName, CreativeWorld creativeWorld) {
        List<CodeBlock> eventBlocks = new java.util.ArrayList<>();
        
        // Get all scripts in the world
        List<CodeScript> scripts = creativeWorld.getScripts();
        if (scripts != null) {
            // Only log if we have scripts to process
            if (!scripts.isEmpty()) {
                getPlugin().getLogger().fine("SimpleScriptTriggerManager: Found " + scripts.size() + " scripts in world " + creativeWorld.getWorldName());
            }
            
            // For each script, check if its root block is an event block that matches our event name
            for (CodeScript script : scripts) {
                CodeBlock rootBlock = script.getRootBlock();
                if (rootBlock != null && isEventBlock(rootBlock, eventName)) {
                    eventBlocks.add(rootBlock);
                    // Add debug logging
                    getPlugin().getLogger().fine("SimpleScriptTriggerManager: Found matching event block with action " + rootBlock.getAction());
                }
            }
        }
        
        return eventBlocks;
    }
    
    /**
     * Check if a block is an event block that matches the given event name
     */
    private boolean isEventBlock(CodeBlock block, String eventName) {
        if (block == null || block.getAction() == null) {
            return false;
        }
        
        // Check if the block action matches the event name
        return block.getAction().equals(eventName);
    }
}