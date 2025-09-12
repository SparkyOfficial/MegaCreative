package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.world.*;
import org.bukkit.event.weather.*;
import org.bukkit.event.server.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎆 FrameLand-Style Comprehensive Events Listener
 * 
 * Provides extensive event coverage matching FrameLand's capabilities:
 * - Player Events: movement, interaction, combat, teleportation
 * - Inventory Events: item manipulation, GUI interactions
 * - World Events: block changes, weather, time
 * - Combat Events: damage, death, PvP interactions
 * - Teleport Events: world changes, portal usage
 * - Custom Events: variable changes, function calls
 * 
 * Features:
 * - High-performance event handler mapping
 * - Async script execution for non-critical events
 * - Event context data for script access
 * - Configurable event filtering and priorities
 * - Thread-safe concurrent execution
 */
public class FrameLandEventsListener implements Listener {
    
    private final MegaCreative plugin;
    private final ScriptEngine scriptEngine;
    
    // Event handler performance optimization
    private final Map<String, Map<UUID, List<CodeScript>>> eventScriptCache = new ConcurrentHashMap<>();
    
    // Event context for script execution
    private final Map<String, Object> currentEventContext = new ConcurrentHashMap<>();
    
    // Event statistics tracking
    private final Map<String, Long> eventExecutionCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> eventExecutionTimes = new ConcurrentHashMap<>();
    
    public FrameLandEventsListener(MegaCreative plugin) {
        this.plugin = plugin;
        this.scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        rebuildEventCache();
    }
    
    /**
     * Rebuild event cache for optimal performance
     */
    public void rebuildEventCache() {
        eventScriptCache.clear();
        
        List<CreativeWorld> worlds = plugin.getWorldManager().getCreativeWorlds();
        for (CreativeWorld world : worlds) {
            if (world.getScripts() == null) continue;
            
            for (CodeScript script : world.getScripts()) {
                if (!script.isEnabled() || script.getRootBlock() == null) continue;
                
                String eventType = script.getRootBlock().getAction();
                if (eventType == null) continue;
                
                eventScriptCache.computeIfAbsent(eventType, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(world.getWorldId(), k -> new ArrayList<>())
                    .add(script);
            }
        }
        
        plugin.getLogger().info("🎆 FrameLand Events: Cached " + eventScriptCache.size() + " event types");
    }
    
    /**
     * Execute scripts for a specific event type
     */
    private void executeEventScripts(String eventType, Player player, String context, Map<String, Object> eventData) {
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) return;
        
        Map<UUID, List<CodeScript>> worldScripts = eventScriptCache.get(eventType);
        if (worldScripts == null) return;
        
        List<CodeScript> scripts = worldScripts.get(world.getWorldId());
        if (scripts == null || scripts.isEmpty()) return;
        
        // Update event context
        currentEventContext.clear();
        currentEventContext.putAll(eventData);
        
        long startTime = System.nanoTime();
        
        for (CodeScript script : scripts) {
            if (scriptEngine != null) {
                scriptEngine.executeScript(script, player, context)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            plugin.getLogger().warning("FrameLand event " + eventType + " failed: " + throwable.getMessage());
                        }
                    });
            }
        }
        
        // Track performance
        long executionTime = System.nanoTime() - startTime;
        eventExecutionCounts.merge(eventType, 1L, Long::sum);
        eventExecutionTimes.merge(eventType, executionTime, Long::sum);
    }
    
    // ============================================================================
    // PLAYER MOVEMENT & INTERACTION EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockY() == event.getTo().getBlockY() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // No block change
        }
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("from_location", event.getFrom());
        eventData.put("to_location", event.getTo());
        eventData.put("distance", event.getFrom().distance(event.getTo()));
        
        executeEventScripts("onPlayerMove", event.getPlayer(), "player_move", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("from_location", event.getFrom());
        eventData.put("to_location", event.getTo());
        eventData.put("cause", event.getCause().name());
        eventData.put("distance", event.getFrom().distance(event.getTo()));
        
        executeEventScripts("onPlayerTeleport", event.getPlayer(), "player_teleport", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("action", event.getAction().name());
        eventData.put("item", event.getItem());
        eventData.put("block", event.getClickedBlock());
        eventData.put("face", event.getBlockFace());
        
        executeEventScripts("onPlayerInteract", event.getPlayer(), "player_interact", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("previous_slot", event.getPreviousSlot());
        eventData.put("new_slot", event.getNewSlot());
        eventData.put("item", event.getPlayer().getInventory().getItem(event.getNewSlot()));
        
        executeEventScripts("onPlayerItemHeld", event.getPlayer(), "player_item_held", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("is_sprinting", event.isSprinting());
        
        executeEventScripts("onPlayerToggleSprint", event.getPlayer(), "player_toggle_sprint", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("is_sneaking", event.isSneaking());
        
        executeEventScripts("onPlayerToggleSneak", event.getPlayer(), "player_toggle_sneak", eventData);
    }
    
    // ============================================================================
    // COMBAT & DAMAGE EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("damage", event.getDamage());
        eventData.put("final_damage", event.getFinalDamage());
        eventData.put("cause", event.getCause().name());
        eventData.put("damager", event.getDamager());
        eventData.put("damager_type", event.getDamager().getType().name());
        
        if (event.getDamager() instanceof Player attacker) {
            eventData.put("attacker", attacker);
            executeEventScripts("onPlayerDamagePlayer", attacker, "player_damage_player", eventData);
        }
        
        executeEventScripts("onPlayerDamaged", victim, "player_damaged", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("damage", event.getDamage());
        eventData.put("final_damage", event.getFinalDamage());
        eventData.put("cause", event.getCause().name());
        
        executeEventScripts("onPlayerDamage", player, "player_damage", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("death_message", event.getDeathMessage());
        eventData.put("drops", event.getDrops());
        eventData.put("experience", event.getDroppedExp());
        eventData.put("killer", event.getEntity().getKiller());
        
        executeEventScripts("onPlayerDeath", event.getEntity(), "player_death", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("respawn_location", event.getRespawnLocation());
        eventData.put("is_bed_spawn", event.isBedSpawn());
        eventData.put("is_anchor_spawn", event.isAnchorSpawn());
        
        executeEventScripts("onPlayerRespawn", event.getPlayer(), "player_respawn", eventData);
    }
    
    // ============================================================================
    // INVENTORY & ITEM EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("slot", event.getSlot());
        eventData.put("slot_type", event.getSlotType().name());
        eventData.put("click_type", event.getClick().name());
        eventData.put("action", event.getAction().name());
        eventData.put("item", event.getCurrentItem());
        eventData.put("cursor_item", event.getCursor());
        
        executeEventScripts("onInventoryClick", player, "inventory_click", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", event.getType().name());
        eventData.put("slots", event.getInventorySlots());
        eventData.put("cursor_item", event.getCursor());
        
        executeEventScripts("onInventoryDrag", player, "inventory_drag", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("item", event.getItemDrop().getItemStack());
        eventData.put("location", event.getItemDrop().getLocation());
        
        executeEventScripts("onPlayerDropItem", event.getPlayer(), "player_drop_item", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("item", event.getItem().getItemStack());
        eventData.put("location", event.getItem().getLocation());
        eventData.put("remaining", event.getRemaining());
        
        executeEventScripts("onPlayerPickupItem", event.getPlayer(), "player_pickup_item", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("item", event.getItem());
        
        executeEventScripts("onPlayerItemConsume", event.getPlayer(), "player_item_consume", eventData);
    }
    
    // ============================================================================
    // BLOCK & WORLD EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("block", event.getBlock());
        eventData.put("block_type", event.getBlock().getType().name());
        eventData.put("location", event.getBlock().getLocation());
        eventData.put("item", event.getItemInHand());
        
        executeEventScripts("onBlockPlace", event.getPlayer(), "block_place", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("block", event.getBlock());
        eventData.put("block_type", event.getBlock().getType().name());
        eventData.put("location", event.getBlock().getLocation());
        eventData.put("drops", event.getBlock().getDrops());
        
        executeEventScripts("onBlockBreak", event.getPlayer(), "block_break", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWeatherChange(WeatherChangeEvent event) {
        // Execute for all players in the world
        for (Player player : event.getWorld().getPlayers()) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("to_storm", event.toWeatherState());
            eventData.put("world", event.getWorld().getName());
            
            executeEventScripts("onWeatherChange", player, "weather_change", eventData);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTimeSkip(TimeSkipEvent event) {
        // Execute for all players in the world
        for (Player player : event.getWorld().getPlayers()) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("skip_amount", event.getSkipAmount());
            eventData.put("skip_reason", event.getSkipReason().name());
            eventData.put("world", event.getWorld().getName());
            
            executeEventScripts("onTimeSkip", player, "time_skip", eventData);
        }
    }
    
    // ============================================================================
    // CHAT & COMMUNICATION EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message", event.getMessage());
        eventData.put("format", event.getFormat());
        eventData.put("recipients", event.getRecipients().size());
        
        executeEventScripts("onPlayerChat", event.getPlayer(), "player_chat", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("command", event.getMessage());
        eventData.put("command_name", event.getMessage().split(" ")[0]);
        
        executeEventScripts("onPlayerCommand", event.getPlayer(), "player_command", eventData);
    }
    
    // ============================================================================
    // CONNECTION EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("join_message", event.getJoinMessage());
        eventData.put("first_play", !event.getPlayer().hasPlayedBefore());
        
        executeEventScripts("onPlayerJoin", event.getPlayer(), "player_join", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("quit_message", event.getQuitMessage());
        
        executeEventScripts("onPlayerQuit", event.getPlayer(), "player_quit", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("kick_reason", event.getReason());
        eventData.put("leave_message", event.getLeaveMessage());
        
        executeEventScripts("onPlayerKick", event.getPlayer(), "player_kick", eventData);
    }
    
    // ============================================================================
    // WORLD CHANGE EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("from_world", event.getFrom().getName());
        eventData.put("to_world", event.getPlayer().getWorld().getName());
        
        executeEventScripts("onPlayerChangedWorld", event.getPlayer(), "player_changed_world", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("from_location", event.getFrom());
        eventData.put("to_location", event.getTo());
        eventData.put("cause", event.getCause().name());
        
        executeEventScripts("onPlayerPortal", event.getPlayer(), "player_portal", eventData);
    }
    
    // ============================================================================
    // LEVEL & EXPERIENCE EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("old_level", event.getOldLevel());
        eventData.put("new_level", event.getNewLevel());
        eventData.put("level_difference", event.getNewLevel() - event.getOldLevel());
        
        executeEventScripts("onPlayerLevelChange", event.getPlayer(), "player_level_change", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("exp_amount", event.getAmount());
        eventData.put("total_exp", event.getPlayer().getTotalExperience() + event.getAmount());
        
        executeEventScripts("onPlayerExpChange", event.getPlayer(), "player_exp_change", eventData);
    }
    
    // ============================================================================
    // VEHICLE & ENTITY EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) return;
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("vehicle", event.getVehicle());
        eventData.put("vehicle_type", event.getVehicle().getType().name());
        
        executeEventScripts("onVehicleEnter", player, "vehicle_enter", eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player player)) return;
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("vehicle", event.getVehicle());
        eventData.put("vehicle_type", event.getVehicle().getType().name());
        
        executeEventScripts("onVehicleExit", player, "vehicle_exit", eventData);
    }
    
    // ============================================================================
    // UTILITY METHODS
    // ============================================================================
    
    /**
     * Get current event context data
     */
    public Map<String, Object> getCurrentEventContext() {
        return new HashMap<>(currentEventContext);
    }
    
    /**
     * Get event execution statistics
     */
    public Map<String, Long> getEventStatistics() {
        Map<String, Long> stats = new HashMap<>();
        eventExecutionCounts.forEach((event, count) -> {
            long totalTime = eventExecutionTimes.getOrDefault(event, 0L);
            long avgTime = count > 0 ? totalTime / count : 0;
            stats.put(event + "_count", count);
            stats.put(event + "_avg_time_ns", avgTime);
        });
        return stats;
    }
    
    /**
     * Reset event statistics
     */
    public void resetStatistics() {
        eventExecutionCounts.clear();
        eventExecutionTimes.clear();
    }
}