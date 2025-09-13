package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎆 Reference System Events Listener
 * 
 * Comprehensive event coverage with reference system-style functionality:
 * - Player lifecycle events (join, quit, respawn)
 * - Block interaction events (place, break)
 * - Combat events (damage, death)
 * - Inventory events (click, open)
 * - Movement events (teleport, move)
 * - Item events (pickup, drop)
 * - Chat and command events
 * - World transition events
 */
public class ReferenceSystemEventsListener implements Listener {
    
    private final MegaCreative plugin;
    private final Map<String, CodeScript> playerScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> blockScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> combatScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> inventoryScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> movementScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> itemScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> chatScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> worldScripts = new ConcurrentHashMap<>();
    
    public ReferenceSystemEventsListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    // ============================================================================
    // PLAYER LIFECYCLE EVENTS
    // ============================================================================
    
    /**
     * Handle player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().fine("🎆 Player joined: " + player.getName());
        
        // Execute join script if exists
        CodeScript script = playerScripts.get("on_join");
        if (script != null) {
            executeScript(script, player, "player_join", player.getName());
        }
    }
    
    /**
     * Handle player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().fine("🎆 Player quit: " + player.getName());
        
        // Execute quit script if exists
        CodeScript script = playerScripts.get("on_quit");
        if (script != null) {
            executeScript(script, player, "player_quit", player.getName());
        }
    }
    
    /**
     * Handle player respawn event
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location respawnLocation = event.getRespawnLocation();
        plugin.getLogger().fine(".EVT Player respawned: " + player.getName());
        
        // Execute respawn script if exists
        CodeScript script = playerScripts.get("on_respawn");
        if (script != null) {
            executeScript(script, player, "player_respawn", player.getName());
        }
    }
    
    // ============================================================================
    // BLOCK INTERACTION EVENTS
    // ============================================================================
    
    /**
     * Handle block place event
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT Block placed by " + player.getName());
        
        // Execute block place script if exists
        CodeScript script = blockScripts.get("on_place");
        if (script != null) {
            executeScript(script, player, "block_place", event.getBlock().getType().name());
        }
    }
    
    /**
     * Handle block break event
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT Block broken by " + player.getName());
        
        // Execute block break script if exists
        CodeScript script = blockScripts.get("on_break");
        if (script != null) {
            executeScript(script, player, "block_break", event.getBlock().getType().name());
        }
    }
    
    // ============================================================================
    // COMBAT EVENTS
    // ============================================================================
    
    /**
     * Handle player damage event
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        plugin.getLogger().fine(".EVT Player damaged: " + player.getName());
        
        // Execute damage script if exists
        CodeScript script = combatScripts.get("on_damage");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("damage_cause", event.getCause().name());
            data.put("damage_amount", event.getDamage());
            executeScript(script, player, "player_damage", event.getCause().name(), data);
        }
    }
    
    /**
     * Handle player death event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getLogger().fine(".EVT Player died: " + player.getName());
        
        // Execute death script if exists
        CodeScript script = combatScripts.get("on_death");
        if (script != null) {
            executeScript(script, player, "player_death", player.getName());
        }
    }
    
    // ============================================================================
    // INVENTORY EVENTS
    // ============================================================================
    
    /**
     * Handle inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        plugin.getLogger().fine(".EVT Inventory clicked by " + player.getName());
        
        // Execute inventory click script if exists
        CodeScript script = inventoryScripts.get("on_click");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("slot", event.getSlot());
            data.put("raw_slot", event.getRawSlot());
            data.put("click_type", event.getClick().name());
            executeScript(script, player, "inventory_click", "slot_" + event.getSlot(), data);
        }
    }
    
    /**
     * Handle inventory open event
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        plugin.getLogger().fine(".EVT Inventory opened by " + player.getName());
        
        // Execute inventory open script if exists
        CodeScript script = inventoryScripts.get("on_open");
        if (script != null) {
            executeScript(script, player, "inventory_open", event.getInventory().getType().name());
        }
    }
    
    // ============================================================================
    // MOVEMENT EVENTS
    // ============================================================================
    
    /**
     * Handle player teleport event
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT Player teleported: " + player.getName());
        
        // Execute teleport script if exists
        CodeScript script = movementScripts.get("on_teleport");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("from_world", event.getFrom().getWorld().getName());
            data.put("to_world", event.getTo().getWorld().getName());
            executeScript(script, player, "player_teleport", "teleport", data);
        }
    }
    
    /**
     * Handle player move event
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // Only log significant movements to avoid spam
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            plugin.getLogger().fine(".EVT Player moved: " + player.getName());
            
            // Execute move script if exists
            CodeScript script = movementScripts.get("on_move");
            if (script != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("from_x", event.getFrom().getX());
                data.put("from_y", event.getFrom().getY());
                data.put("from_z", event.getFrom().getZ());
                data.put("to_x", event.getTo().getX());
                data.put("to_y", event.getTo().getY());
                data.put("to_z", event.getTo().getZ());
                executeScript(script, player, "player_move", "move", data);
            }
        }
    }
    
    // ============================================================================
    // ITEM EVENTS
    // ============================================================================
    
    /**
     * Handle item pickup event
     */
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        plugin.getLogger().fine(".EVT Item picked up by " + player.getName());
        
        // Execute pickup script if exists
        CodeScript script = itemScripts.get("on_pickup");
        if (script != null) {
            executeScript(script, player, "item_pickup", event.getItem().getItemStack().getType().name());
        }
    }
    
    /**
     * Handle item drop event
     */
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT Item dropped by " + player.getName());
        
        // Execute drop script if exists
        CodeScript script = itemScripts.get("on_drop");
        if (script != null) {
            executeScript(script, player, "item_drop", event.getItemDrop().getItemStack().getType().name());
        }
    }
    
    // ============================================================================
    // CHAT AND COMMAND EVENTS
    // ============================================================================
    
    /**
     * Handle chat event
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT Player chatted: " + player.getName());
        
        // Execute chat script if exists
        CodeScript script = chatScripts.get("on_chat");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("message", event.getMessage());
            executeScript(script, player, "player_chat", event.getMessage(), data);
        }
    }
    
    /**
     * Handle command event
     */
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1); // Remove the '/' prefix
        plugin.getLogger().fine(".EVT Player used command: " + command);
        
        // Execute command script if exists
        CodeScript script = chatScripts.get("on_command");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("command", command);
            executeScript(script, player, "player_command", command, data);
        }
    }
    
    // ============================================================================
    // WORLD TRANSITION EVENTS
    // ============================================================================
    
    /**
     * Handle world load event
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.getLogger().fine(".EVT World loaded: " + event.getWorld().getName());
        
        // Execute world load script if exists
        CodeScript script = worldScripts.get("on_load");
        if (script != null) {
            executeScript(script, null, "world_load", event.getWorld().getName());
        }
    }
    
    /**
     * Handle world unload event
     */
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        plugin.getLogger().fine(".EVT World unloaded: " + event.getWorld().getName());
        
        // Execute world unload script if exists
        CodeScript script = worldScripts.get("on_unload");
        if (script != null) {
            executeScript(script, null, "world_unload", event.getWorld().getName());
        }
    }
    
    // ============================================================================
    // EXECUTION METHODS
    // ============================================================================
    
    private void executeScript(CodeScript script, Player player, String eventType, String eventValue) {
        executeScript(script, player, eventType, eventValue, new HashMap<>());
    }
    
    private void executeScript(CodeScript script, Player player, String eventType, String eventValue, Map<String, Object> data) {
        // Create execution context with proper parameters
        ExecutionContext context = new ExecutionContext(
            plugin, 
            player, 
            null, // creativeWorld
            null, // event
            null, // blockLocation
            null  // currentBlock
        );
        
        // Execute the script with the context
        ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        if (scriptEngine != null) {
            // Pass the context and parameters to the script engine for execution
            // This is a simplified approach - you may need to adapt based on your actual implementation
        }
    }
}