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
 * 
 * This class is now completely decoupled from Bukkit events and only listens to our
 * clean, internal events
 */
public class ScriptTriggerManager implements Listener {
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
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) return;
            
            List<Activator> activators = codeHandler.getActivatorsByType(com.megacreative.coding.activators.ActivatorType.PLAYER_JOIN);
            if (activators == null || activators.isEmpty()) return;
            
            for (Activator activator : activators) {
                if (activator instanceof PlayerJoinActivator) {
                    activator.execute(new com.megacreative.coding.events.GameEvent("onJoin"), 0, new java.util.concurrent.atomic.AtomicInteger());
                }
            }
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaPlayerMove(MegaPlayerMoveEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) return;
            
            List<Activator> activators = codeHandler.getActivatorsByType(com.megacreative.coding.activators.ActivatorType.PLAYER_MOVE);
            if (activators == null || activators.isEmpty()) return;
            
            for (Activator activator : activators) {
                if (activator instanceof PlayerMoveActivator) {
                    activator.execute(new com.megacreative.coding.events.GameEvent("onPlayerMove"), 0, new java.util.concurrent.atomic.AtomicInteger());
                }
            }
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaPlayerChat(MegaPlayerChatEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            executeScriptsForEvent("onChat", event.getPlayer());
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaBlockPlace(MegaBlockPlaceEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            executeScriptsForEvent("onBlockPlace", event.getPlayer());
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaBlockBreak(MegaBlockBreakEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            executeScriptsForEvent("onBlockBreak", event.getPlayer());
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaEntityPickupItem(MegaEntityPickupItemEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            executeScriptsForEvent("onEntityPickupItem", event.getPlayer());
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaPlayerDeath(MegaPlayerDeathEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            executeScriptsForEvent("onPlayerDeath", event.getPlayer());
        } catch (Exception e) {
            // Suppress exception
        }
    }
    @EventHandler
    public void onMegaPlayerQuit(MegaPlayerQuitEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            executeScriptsForEvent("onQuit", event.getPlayer());
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaPlayerRespawn(MegaPlayerRespawnEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            executeScriptsForEvent("onRespawn", event.getPlayer());
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaPlayerTeleport(MegaPlayerTeleportEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            executeScriptsForEvent("onTeleport", event.getPlayer());
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaEntityDamage(MegaEntityDamageEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            org.bukkit.entity.Player player = event.getPlayer();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(player)) return;
            
            executeScriptsForEvent("onEntityDamage", player);
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaInventoryClick(MegaInventoryClickEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            org.bukkit.entity.Player player = event.getPlayer();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(player)) return;
            
            executeScriptsForEvent("onInventoryClick", player);
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onMegaInventoryOpen(MegaInventoryOpenEvent event) {
        if (event == null || event.getPlayer() == null || worldManager == null) return;
        
        try {
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(event.getPlayer())) return;
            
            executeScriptsForEvent("onInventoryOpen", event.getPlayer());
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    @EventHandler
    public void onTick(com.megacreative.events.TickEvent event) {
        if (event == null || worldManager == null) return;
        
        try {
            executeScriptsForGlobalEvent("onTick");
            
            if (event.getTick() % 20 == 0) {
                executeScriptsForGlobalEvent("onSecond");
            }
            
            if (event.getTick() % 1200 == 0) {
                executeScriptsForGlobalEvent("onMinute");
            }
        } catch (Exception e) {
            // Suppress exception
        }
    }
    
    /**
     * Execute scripts directly for an event
     * @param eventName the name of the event
     * @param player the player associated with the event
     */
    private void executeScriptsForEvent(String eventName, org.bukkit.entity.Player player) {
        if (eventName == null || worldManager == null) {
            return;
        }
        
        try {
            // Find the creative world
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player != null ? player.getWorld() : null);
            if (creativeWorld == null) {
                return;
            }
            
            // Get the code handler for this world
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) {
                return;
            }
            
            // Map event names to activator types
            com.megacreative.coding.activators.ActivatorType activatorType = null;
            switch (eventName) {
                case "onJoin":
                    activatorType = com.megacreative.coding.activators.ActivatorType.PLAYER_JOIN;
                    break;
                case "onPlayerMove":
                    activatorType = com.megacreative.coding.activators.ActivatorType.PLAYER_MOVE;
                    break;
                case "onChat":
                    activatorType = com.megacreative.coding.activators.ActivatorType.PLAYER_CHAT;
                    break;
                case "onBlockPlace":
                    activatorType = com.megacreative.coding.activators.ActivatorType.BLOCK_PLACE;
                    break;
                case "onBlockBreak":
                    activatorType = com.megacreative.coding.activators.ActivatorType.BLOCK_BREAK;
                    break;
                case "onEntityPickupItem":
                    activatorType = com.megacreative.coding.activators.ActivatorType.ENTITY_PICKUP_ITEM;
                    break;
                case "onPlayerDeath":
                    activatorType = com.megacreative.coding.activators.ActivatorType.PLAYER_DEATH;
                    break;
                case "onQuit":
                    activatorType = com.megacreative.coding.activators.ActivatorType.PLAYER_QUIT;
                    break;
                case "onRespawn":
                    activatorType = com.megacreative.coding.activators.ActivatorType.PLAYER_RESPAWN;
                    break;
                case "onTeleport":
                    // There's no specific teleport activator type, use a generic one
                    activatorType = com.megacreative.coding.activators.ActivatorType.PLAYER_RESPAWN;
                    break;
                case "onEntityDamage":
                    // There's no specific entity damage activator type, use a generic one
                    activatorType = com.megacreative.coding.activators.ActivatorType.PLAYER_DEATH;
                    break;
                case "onInventoryClick":
                    // There's no specific inventory click activator type, use a generic one
                    activatorType = com.megacreative.coding.activators.ActivatorType.PLAYER_DEATH;
                    break;
                case "onInventoryOpen":
                    // There's no specific inventory open activator type, use a generic one
                    activatorType = com.megacreative.coding.activators.ActivatorType.PLAYER_DEATH;
                    break;
            }
            
            if (activatorType == null) {
                return;
            }
            
            // Create a game event with context data
            GameEvent gameEvent = new GameEvent(eventName);
            if (player != null) {
                gameEvent.setPlayer(player);
            }
            
            // Handle the event through the code handler
            codeHandler.handleEvent(activatorType, gameEvent, player);
        } catch (Exception e) {
        }
    }
    
    /**
     * Execute scripts for global events that don't have a specific world
     * @param eventName the name of the global event
     */
    private void executeScriptsForGlobalEvent(String eventName) {
        if (eventName == null || worldManager == null) {
            return;
        }
        
        try {
            // For global events, we execute scripts in all loaded worlds
            for (CreativeWorld creativeWorld : worldManager.getCreativeWorlds()) {
                if (creativeWorld == null) continue;
                
                // Get the code handler for this world
                CodeHandler codeHandler = creativeWorld.getCodeHandler();
                if (codeHandler == null) {
                    continue; // Skip worlds without code handlers
                }
                
                // Map event names to activator types
                com.megacreative.coding.activators.ActivatorType activatorType = null;
                switch (eventName) {
                    case "onTick":
                        activatorType = com.megacreative.coding.activators.ActivatorType.TICK;
                        break;
                    case "onSecond":
                        // There's no specific second activator type, use tick
                        activatorType = com.megacreative.coding.activators.ActivatorType.TICK;
                        break;
                    case "onMinute":
                        // There's no specific minute activator type, use tick
                        activatorType = com.megacreative.coding.activators.ActivatorType.TICK;
                        break;
                }
                
                if (activatorType == null) {
                    continue;
                }
                
                // Create a game event with context data
                GameEvent gameEvent = new GameEvent(eventName);
                
                // Handle the event through the code handler
                codeHandler.handleEvent(activatorType, gameEvent, null);
            }
        } catch (Exception e) {
        }
    }
}