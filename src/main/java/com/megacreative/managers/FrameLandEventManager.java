package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.events.FrameLandCustomEvents.*;
import com.megacreative.listeners.FrameLandEventsListener;
import com.megacreative.listeners.FrameLandCustomEventsListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎆 FrameLand Event Manager
 * 
 * Coordinates comprehensive event system with FrameLand-style functionality:
 * - Manages both standard and custom events
 * - Handles region tracking and detection
 * - Variable change monitoring
 * - Timer system for delayed events
 * - Event analytics and performance monitoring
 * - Custom event triggering and management
 */
public class FrameLandEventManager {
    
    private final MegaCreative plugin;
    private final FrameLandEventsListener standardEventsListener;
    private final FrameLandCustomEventsListener customEventsListener;
    
    // Region tracking for enter/leave events
    private final Map<UUID, Set<String>> playerRegions = new ConcurrentHashMap<>();
    private final Map<String, RegionData> definedRegions = new ConcurrentHashMap<>();
    
    // Variable monitoring
    private final Map<UUID, Map<String, Object>> playerVariables = new ConcurrentHashMap<>();
    
    // Timer system
    private final Map<String, BukkitTask> activeTimers = new ConcurrentHashMap<>();
    private final Map<String, TimerData> timerData = new ConcurrentHashMap<>();
    
    // Event analytics
    private final Map<String, Long> eventCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> eventTotalTime = new ConcurrentHashMap<>();
    
    public FrameLandEventManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.standardEventsListener = new FrameLandEventsListener(plugin);
        this.customEventsListener = new FrameLandCustomEventsListener(plugin);
        
        // Register listeners
        Bukkit.getPluginManager().registerEvents(standardEventsListener, plugin);
        Bukkit.getPluginManager().registerEvents(customEventsListener, plugin);
        
        // Start region monitoring task
        startRegionMonitoring();
        
        plugin.getLogger().info("🎆 FrameLand Event Manager initialized with comprehensive event coverage");
    }
    
    // ============================================================================
    // REGION MANAGEMENT
    // ============================================================================
    
    /**
     * Define a new region for enter/leave event detection
     */
    public void defineRegion(String regionName, Location corner1, Location corner2) {
        RegionData region = new RegionData(regionName, corner1, corner2);
        definedRegions.put(regionName, region);
        plugin.getLogger().info("🎆 Defined region: " + regionName);
    }
    
    /**
     * Remove a region definition
     */
    public void removeRegion(String regionName) {
        definedRegions.remove(regionName);
        // Remove all players from this region
        playerRegions.values().forEach(regions -> regions.remove(regionName));
        plugin.getLogger().info("🎆 Removed region: " + regionName);
    }
    
    /**
     * Check if a location is within a region
     */
    public boolean isLocationInRegion(Location location, String regionName) {
        RegionData region = definedRegions.get(regionName);
        if (region == null) return false;
        
        return location.getWorld().equals(region.corner1.getWorld()) &&
               location.getX() >= region.minX && location.getX() <= region.maxX &&
               location.getY() >= region.minY && location.getY() <= region.maxY &&
               location.getZ() >= region.minZ && location.getZ() <= region.maxZ;
    }
    
    /**
     * Start region monitoring task
     */
    private void startRegionMonitoring() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkPlayerRegions(player);
            }
        }, 20L, 20L); // Check every second
    }
    
    /**
     * Check if player has entered or left any regions
     */
    private void checkPlayerRegions(Player player) {
        UUID playerId = player.getUniqueId();
        Set<String> currentRegions = playerRegions.computeIfAbsent(playerId, k -> new HashSet<>());
        Set<String> newRegions = new HashSet<>();
        
        Location playerLoc = player.getLocation();
        
        // Check all defined regions
        for (String regionName : definedRegions.keySet()) {
            if (isLocationInRegion(playerLoc, regionName)) {
                newRegions.add(regionName);
                
                // If player wasn't in this region before, fire enter event
                if (!currentRegions.contains(regionName)) {
                    PlayerEnterRegionEvent event = new PlayerEnterRegionEvent(player, regionName, playerLoc);
                    Bukkit.getPluginManager().callEvent(event);
                }
            }
        }
        
        // Check for regions the player left
        for (String regionName : currentRegions) {
            if (!newRegions.contains(regionName)) {
                PlayerLeaveRegionEvent event = new PlayerLeaveRegionEvent(player, regionName, playerLoc);
                Bukkit.getPluginManager().callEvent(event);
            }
        }
        
        // Update player's current regions
        playerRegions.put(playerId, newRegions);
    }
    
    // ============================================================================
    // VARIABLE MONITORING
    // ============================================================================
    
    /**
     * Monitor a variable for changes
     */
    public void monitorVariable(Player player, String variableName, Object currentValue) {
        UUID playerId = player.getUniqueId();
        Map<String, Object> variables = playerVariables.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        
        Object oldValue = variables.get(variableName);
        if (!Objects.equals(oldValue, currentValue)) {
            variables.put(variableName, currentValue);
            
            // Fire variable change event
            PlayerVariableChangeEvent event = new PlayerVariableChangeEvent(player, variableName, oldValue, currentValue);
            Bukkit.getPluginManager().callEvent(event);
        }
    }
    
    /**
     * Get current value of a monitored variable
     */
    public Object getMonitoredVariable(Player player, String variableName) {
        return playerVariables.getOrDefault(player.getUniqueId(), new HashMap<>()).get(variableName);
    }
    
    /**
     * Clear all monitored variables for a player
     */
    public void clearMonitoredVariables(Player player) {
        playerVariables.remove(player.getUniqueId());
    }
    
    // ============================================================================
    // TIMER SYSTEM
    // ============================================================================
    
    /**
     * Start a timer that will fire an event when it expires
     */
    public void startTimer(Player player, String timerName, long delayTicks, Object timerData) {
        // Cancel existing timer with same name
        stopTimer(timerName);
        
        TimerData data = new TimerData(player, timerName, delayTicks, timerData);
        this.timerData.put(timerName, data);
        
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Fire timer expire event
            TimerExpireEvent event = new TimerExpireEvent(player, timerName, delayTicks, timerData);
            Bukkit.getPluginManager().callEvent(event);
            
            // Clean up
            activeTimers.remove(timerName);
            this.timerData.remove(timerName);
        }, delayTicks);
        
        activeTimers.put(timerName, task);
        plugin.getLogger().fine("🎆 Started timer: " + timerName + " for " + delayTicks + " ticks");
    }
    
    /**
     * Stop a timer
     */
    public void stopTimer(String timerName) {
        BukkitTask task = activeTimers.remove(timerName);
        if (task != null) {
            task.cancel();
            timerData.remove(timerName);
            plugin.getLogger().fine("🎆 Stopped timer: " + timerName);
        }
    }
    
    /**
     * Check if a timer is active
     */
    public boolean isTimerActive(String timerName) {
        return activeTimers.containsKey(timerName);
    }
    
    /**
     * Get remaining time for a timer (in ticks)
     */
    public long getTimerRemainingTime(String timerName) {
        TimerData data = timerData.get(timerName);
        if (data == null) return -1;
        
        long elapsed = System.currentTimeMillis() - data.startTime;
        long elapsedTicks = elapsed / 50; // Convert to ticks (20 TPS)
        return Math.max(0, data.duration - elapsedTicks);
    }
    
    // ============================================================================
    // CUSTOM EVENT TRIGGERING
    // ============================================================================
    
    /**
     * Trigger a custom action event
     */
    public void triggerCustomAction(Player player, String actionName, Map<String, Object> actionData) {
        PlayerCustomActionEvent event = new PlayerCustomActionEvent(player, actionName, actionData);
        Bukkit.getPluginManager().callEvent(event);
    }
    
    /**
     * Trigger a score change event
     */
    public void triggerScoreChange(Player player, String scoreType, int oldScore, int newScore, String reason) {
        PlayerScoreChangeEvent event = new PlayerScoreChangeEvent(player, scoreType, oldScore, newScore, reason);
        Bukkit.getPluginManager().callEvent(event);
    }
    
    /**
     * Trigger a function call event
     */
    public void triggerFunctionCall(Player player, String functionName, Object[] parameters) {
        FunctionCallEvent event = new FunctionCallEvent(player, functionName, parameters);
        Bukkit.getPluginManager().callEvent(event);
    }
    
    /**
     * Trigger a world mode change event
     */
    public void triggerWorldModeChange(Player player, String worldId, String oldMode, String newMode) {
        WorldModeChangeEvent event = new WorldModeChangeEvent(player, worldId, oldMode, newMode);
        Bukkit.getPluginManager().callEvent(event);
    }
    
    // ============================================================================
    // EVENT ANALYTICS
    // ============================================================================
    
    /**
     * Track event execution statistics
     */
    public void trackEventExecution(String eventType, long executionTimeNanos) {
        eventCounts.merge(eventType, 1L, Long::sum);
        eventTotalTime.merge(eventType, executionTimeNanos, Long::sum);
    }
    
    /**
     * Get event execution statistics
     */
    public Map<String, Object> getEventStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        for (String eventType : eventCounts.keySet()) {
            long count = eventCounts.get(eventType);
            long totalTime = eventTotalTime.getOrDefault(eventType, 0L);
            long avgTime = count > 0 ? totalTime / count : 0;
            
            Map<String, Object> eventStats = new HashMap<>();
            eventStats.put("count", count);
            eventStats.put("total_time_ns", totalTime);
            eventStats.put("avg_time_ns", avgTime);
            eventStats.put("avg_time_ms", avgTime / 1_000_000.0);
            
            stats.put(eventType, eventStats);
        }
        
        return stats;
    }
    
    /**
     * Reset event statistics
     */
    public void resetStatistics() {
        eventCounts.clear();
        eventTotalTime.clear();
    }
    
    // ============================================================================
    // CLEANUP AND SHUTDOWN
    // ============================================================================
    
    /**
     * Cleanup resources
     */
    public void shutdown() {
        // Cancel all active timers
        activeTimers.values().forEach(BukkitTask::cancel);
        activeTimers.clear();
        timerData.clear();
        
        // Clear all tracking data
        playerRegions.clear();
        playerVariables.clear();
        definedRegions.clear();
        
        plugin.getLogger().info("🎆 FrameLand Event Manager shut down");
    }
    
    /**
     * Refresh event caches
     */
    public void refreshEventCaches() {
        standardEventsListener.rebuildEventCache();
        customEventsListener.rebuildCustomEventCache();
    }
    
    // ============================================================================
    // HELPER CLASSES
    // ============================================================================
    
    /**
     * Region data structure
     */
    private static class RegionData {
        final String name;
        final Location corner1;
        final Location corner2;
        final double minX, maxX, minY, maxY, minZ, maxZ;
        
        RegionData(String name, Location corner1, Location corner2) {
            this.name = name;
            this.corner1 = corner1;
            this.corner2 = corner2;
            
            this.minX = Math.min(corner1.getX(), corner2.getX());
            this.maxX = Math.max(corner1.getX(), corner2.getX());
            this.minY = Math.min(corner1.getY(), corner2.getY());
            this.maxY = Math.max(corner1.getY(), corner2.getY());
            this.minZ = Math.min(corner1.getZ(), corner2.getZ());
            this.maxZ = Math.max(corner1.getZ(), corner2.getZ());
        }
    }
    
    /**
     * Timer data structure
     */
    private static class TimerData {
        final Player player;
        final String name;
        final long duration;
        final Object data;
        final long startTime;
        
        TimerData(Player player, String name, long duration, Object data) {
            this.player = player;
            this.name = name;
            this.duration = duration;
            this.data = data;
            this.startTime = System.currentTimeMillis();
        }
    }
}