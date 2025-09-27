package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.events.*;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.logging.Logger;

/**
 * Script trigger manager that listens to our custom events and triggers script execution
 * This replaces the old PlayerEventsListener which listened directly to Bukkit events
 * 
 * This class is now completely decoupled from Bukkit events and only listens to our
 * clean, internal events
 */
public class ScriptTriggerManager implements Listener {
    private static final Logger LOGGER = Logger.getLogger(ScriptTriggerManager.class.getName());
    
    private final MegaCreative plugin;
    private final ScriptEngine scriptEngine;
    private final IWorldManager worldManager;
    private final PlayerModeManager playerModeManager;
    
    public ScriptTriggerManager(MegaCreative plugin, ScriptEngine scriptEngine, 
                               IWorldManager worldManager, PlayerModeManager playerModeManager) {
        this.plugin = plugin;
        this.scriptEngine = scriptEngine;
        this.worldManager = worldManager;
        this.playerModeManager = playerModeManager;
    }
    
    @EventHandler
    public void onMegaPlayerJoin(MegaPlayerJoinedEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onJoin event in both PLAY and DEV modes
        executeScriptsForEvent("onJoin", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaPlayerMove(MegaPlayerMoveEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onPlayerMove event in both PLAY and DEV modes
        executeScriptsForEvent("onPlayerMove", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaPlayerChat(MegaPlayerChatEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onChat event in both PLAY and DEV modes
        executeScriptsForEvent("onChat", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaBlockPlace(MegaBlockPlaceEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onBlockPlace event in both PLAY and DEV modes
        executeScriptsForEvent("onBlockPlace", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaBlockBreak(MegaBlockBreakEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onBlockBreak event in both PLAY and DEV modes
        executeScriptsForEvent("onBlockBreak", event.getPlayer());
    }
    
    @EventHandler
    public void onTick(com.megacreative.events.TickEvent event) {
        // Execute scripts for onTick event
        // This will trigger all scripts that start with EVENT_TICK
        executeScriptsForEvent("onTick", null);
        
        // Every 20 ticks (1 second), also trigger EVENT_SECOND scripts
        if (event.getTick() % 20 == 0) {
            executeScriptsForEvent("onSecond", null);
        }
        
        // Every 1200 ticks (1 minute), also trigger EVENT_MINUTE scripts
        if (event.getTick() % 1200 == 0) {
            executeScriptsForEvent("onMinute", null);
        }
    }
    
    /**
     * Execute scripts directly for an event
     * @param eventName the name of the event
     * @param player the player associated with the event
     */
    private void executeScriptsForEvent(String eventName, org.bukkit.entity.Player player) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player != null ? player.getWorld() : null);
        if (creativeWorld == null) {
            LOGGER.warning("No creative world found for event: " + eventName);
            return;
        }
        
        // Get the code handler for this world
        CodeHandler codeHandler = creativeWorld.getCodeHandler();
        if (codeHandler == null) {
            LOGGER.warning("No code handler found for world: " + creativeWorld.getId());
            return;
        }
        
        // Create a game event with context data
        GameEvent gameEvent = new GameEvent(eventName);
        if (player != null) {
            gameEvent.setPlayer(player);
        }
        
        // Handle the event through the code handler
        codeHandler.handleEvent(eventName, gameEvent, player);
    }
}