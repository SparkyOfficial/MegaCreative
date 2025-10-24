package com.megacreative.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.events.CustomEventManager.EventHandler;
import com.megacreative.coding.values.DataValue;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Event handler for player join events using the CustomEventManager system
 * This replaces the PlayerJoinActivator functionality
 */
public class PlayerJoinEventHandler {
    private static final Logger LOGGER = Logger.getLogger(PlayerJoinEventHandler.class.getName());
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final CustomEventManager eventManager;
    
    public PlayerJoinEventHandler(MegaCreative plugin, IWorldManager worldManager, CustomEventManager eventManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.eventManager = eventManager;
        
        
        registerEventHandler();
    }
    
    /**
     * Register this handler with the CustomEventManager
     */
    private void registerEventHandler() {
        
        EventHandler handler = new EventHandler(
            null, 
            null, 
            null, 
            0,    
            plugin
        ) {
            @Override
            public boolean canHandle(Player source, String sourceWorld, Map<String, DataValue> eventData) {
                return true; 
            }
            
            @Override
            public void handle(Map<String, DataValue> eventData, Player source, String sourceWorld) {
                handlePlayerJoin(eventData, source, sourceWorld);
            }
        };
        
        
        eventManager.registerEventHandler("playerConnect", handler);
        LOGGER.fine("Registered PlayerJoinEventHandler for playerConnect events");
    }
    
    /**
     * Handle player join events
     */
    private void handlePlayerJoin(Map<String, DataValue> eventData, Player player, String worldName) {
        if (player == null || worldManager == null) {
            return;
        }
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(player)) {
                return;
            }
            
            
            executeScriptsForPlayerJoin(player, creativeWorld);
        } catch (Exception e) {
            LOGGER.warning("Error handling player join event: " + e.getMessage());
        }
    }
    
    /**
     * Execute scripts for player join event
     */
    private void executeScriptsForPlayerJoin(Player player, CreativeWorld creativeWorld) {
        
        
        LOGGER.fine("Would execute scripts for player join: " + player.getName() + " in world " + creativeWorld.getName());
    }
}