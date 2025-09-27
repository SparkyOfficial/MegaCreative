package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.activators.Activator;
import com.megacreative.coding.activators.PlayerJoinActivator;
import com.megacreative.coding.activators.PlayerMoveActivator;
import com.megacreative.coding.activators.BlockPlaceActivator;
import com.megacreative.coding.activators.BlockBreakActivator;
import com.megacreative.coding.activators.ChatActivator;
import com.megacreative.coding.activators.EntityDamageActivator;
import com.megacreative.coding.activators.EntityPickupItemActivator;
import com.megacreative.coding.activators.InventoryClickActivator;
import com.megacreative.coding.activators.PlayerDeathActivator;
import com.megacreative.coding.activators.PlayerQuitActivator;
import com.megacreative.coding.activators.PlayerRespawnActivator;
import com.megacreative.coding.activators.PlayerTeleportActivator;
import com.megacreative.coding.activators.TickActivator;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.events.*;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.List;
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
    private final IWorldManager worldManager;
    private final PlayerModeManager playerModeManager;
    
    public ScriptTriggerManager(MegaCreative plugin, IWorldManager worldManager, PlayerModeManager playerModeManager) {
        this.plugin = plugin;
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
        
        // Find the code handler for this world
        CodeHandler codeHandler = creativeWorld.getCodeHandler();
        if (codeHandler == null) return;
        
        // Get all activators for this event
        List<Activator> activators = codeHandler.getActivatorsForEvent("onJoin");
        if (activators.isEmpty()) return;
        
        // Execute each activator with proper context data
        for (Activator activator : activators) {
            if (activator instanceof PlayerJoinActivator && activator.isEnabled() && activator.getScript() != null) {
                PlayerJoinActivator playerJoinActivator = (PlayerJoinActivator) activator;
                playerJoinActivator.activate(event.getPlayer(), event.isFirstJoin());
            }
        }
    }
    
    @EventHandler
    public void onMegaPlayerMove(MegaPlayerMoveEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Find the code handler for this world
        CodeHandler codeHandler = creativeWorld.getCodeHandler();
        if (codeHandler == null) return;
        
        // Get all activators for this event
        List<Activator> activators = codeHandler.getActivatorsForEvent("onPlayerMove");
        if (activators.isEmpty()) return;
        
        // Execute each activator with proper context data
        for (Activator activator : activators) {
            if (activator instanceof PlayerMoveActivator && activator.isEnabled() && activator.getScript() != null) {
                PlayerMoveActivator playerMoveActivator = (PlayerMoveActivator) activator;
                playerMoveActivator.activate(event.getPlayer(), event.getFrom(), event.getTo());
            }
        }
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
    public void onMegaEntityPickupItem(MegaEntityPickupItemEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onEntityPickupItem event in both PLAY and DEV modes
        executeScriptsForEvent("onEntityPickupItem", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaPlayerDeath(MegaPlayerDeathEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onPlayerDeath event in both PLAY and DEV modes
        executeScriptsForEvent("onPlayerDeath", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaPlayerQuit(MegaPlayerQuitEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onPlayerQuit event in both PLAY and DEV modes
        executeScriptsForEvent("onPlayerQuit", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaPlayerRespawn(MegaPlayerRespawnEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onPlayerRespawn event in both PLAY and DEV modes
        executeScriptsForEvent("onPlayerRespawn", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaPlayerTeleport(MegaPlayerTeleportEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onPlayerTeleport event in both PLAY and DEV modes
        executeScriptsForEvent("onPlayerTeleport", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaEntityDamage(MegaEntityDamageEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onEntityDamage event in both PLAY and DEV modes
        executeScriptsForEvent("onEntityDamage", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaInventoryClick(MegaInventoryClickEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onInventoryClick event in both PLAY and DEV modes
        executeScriptsForEvent("onInventoryClick", event.getPlayer());
    }
    
    @EventHandler
    public void onMegaInventoryOpen(MegaInventoryOpenEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Execute scripts for onInventoryOpen event in both PLAY and DEV modes
        executeScriptsForEvent("onInventoryOpen", event.getPlayer());
    }
    
    @EventHandler
    public void onTick(com.megacreative.events.TickEvent event) {
        // Execute scripts for onTick event
        // This will trigger all scripts that start with EVENT_TICK
        executeScriptsForGlobalEvent("onTick");
        
        // Every 20 ticks (1 second), also trigger EVENT_SECOND scripts
        if (event.getTick() % 20 == 0) {
            executeScriptsForGlobalEvent("onSecond");
        }
        
        // Every 1200 ticks (1 minute), also trigger EVENT_MINUTE scripts
        if (event.getTick() % 1200 == 0) {
            executeScriptsForGlobalEvent("onMinute");
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
    
    /**
     * Execute scripts for global events that don't have a specific world
     * @param eventName the name of the global event
     */
    private void executeScriptsForGlobalEvent(String eventName) {
        // For global events, we execute scripts in all loaded worlds
        for (CreativeWorld creativeWorld : worldManager.getCreativeWorlds()) {
            // Get the code handler for this world
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) {
                continue; // Skip worlds without code handlers
            }
            
            // Create a game event with context data
            GameEvent gameEvent = new GameEvent(eventName);
            
            // Handle the event through the code handler
            codeHandler.handleEvent(eventName, gameEvent, null);
        }
    }
}