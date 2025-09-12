package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import com.megacreative.events.FrameLandCustomEvents.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎆 FrameLand Custom Events Listener
 * 
 * Handles custom FrameLand-style events for enhanced scripting capabilities:
 * - Variable change events
 * - Region enter/leave events
 * - Function call events
 * - Score change events
 * - Custom action events
 * - Timer events
 * - World mode change events
 */
public class FrameLandCustomEventsListener implements Listener {
    
    private final MegaCreative plugin;
    private final ScriptEngine scriptEngine;
    
    // Event handler cache for performance
    private final Map<String, Map<UUID, List<CodeScript>>> customEventScriptCache = new ConcurrentHashMap<>();
    
    public FrameLandCustomEventsListener(MegaCreative plugin) {
        this.plugin = plugin;
        this.scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        rebuildCustomEventCache();
    }
    
    /**
     * Rebuild custom event cache for optimal performance
     */
    public void rebuildCustomEventCache() {
        customEventScriptCache.clear();
        
        List<CreativeWorld> worlds = plugin.getWorldManager().getCreativeWorlds();
        for (CreativeWorld world : worlds) {
            if (world.getScripts() == null) continue;
            
            for (CodeScript script : world.getScripts()) {
                if (!script.isEnabled() || script.getRootBlock() == null) continue;
                
                String eventType = script.getRootBlock().getAction();
                if (eventType == null) continue;
                
                // Only cache custom event types
                if (isCustomEventType(eventType)) {
                    customEventScriptCache.computeIfAbsent(eventType, k -> new ConcurrentHashMap<>())
                        .computeIfAbsent(world.getWorldId(), k -> new ArrayList<>())
                        .add(script);
                }
            }
        }
        
        plugin.getLogger().info("🎆 FrameLand Custom Events: Cached " + customEventScriptCache.size() + " custom event types");
    }
    
    /**
     * Check if an event type is a custom FrameLand event
     */
    private boolean isCustomEventType(String eventType) {
        return eventType.startsWith("onVariable") || 
               eventType.startsWith("onRegion") || 
               eventType.startsWith("onFunction") || 
               eventType.startsWith("onScore") || 
               eventType.startsWith("onCustom") || 
               eventType.startsWith("onTimer") || 
               eventType.startsWith("onWorldMode");
    }
    
    /**
     * Execute scripts for a custom event type
     */
    private void executeCustomEventScripts(String eventType, Player player, String context, Map<String, Object> eventData) {
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) return;
        
        Map<UUID, List<CodeScript>> worldScripts = customEventScriptCache.get(eventType);
        if (worldScripts == null) return;
        
        List<CodeScript> scripts = worldScripts.get(world.getWorldId());
        if (scripts == null || scripts.isEmpty()) return;
        
        for (CodeScript script : scripts) {
            if (scriptEngine != null) {
                // Add event data to script context
                script.getContext().putAll(eventData);
                
                scriptEngine.executeScript(script, player, context)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            plugin.getLogger().warning("FrameLand custom event " + eventType + " failed: " + throwable.getMessage());
                        }
                    });
            }
        }
    }
    
    // ============================================================================
    // VARIABLE CHANGE EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerVariableChange(PlayerVariableChangeEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("variable_name", event.getVariableName());
        eventData.put("old_value", event.getOldValue());
        eventData.put("new_value", event.getNewValue());
        eventData.put("value_type", event.getNewValue() != null ? event.getNewValue().getClass().getSimpleName() : "null");
        
        // Execute both generic and specific variable change events
        executeCustomEventScripts("onVariableChange", event.getPlayer(), "variable_change", eventData);
        executeCustomEventScripts("onVariable" + event.getVariableName(), event.getPlayer(), "variable_" + event.getVariableName(), eventData);
    }
    
    // ============================================================================
    // REGION EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerEnterRegion(PlayerEnterRegionEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("region_name", event.getRegionName());
        eventData.put("enter_location", event.getEnterLocation());
        eventData.put("world", event.getEnterLocation().getWorld().getName());
        
        executeCustomEventScripts("onRegionEnter", event.getPlayer(), "region_enter", eventData);
        executeCustomEventScripts("onRegionEnter" + event.getRegionName(), event.getPlayer(), "region_enter_" + event.getRegionName(), eventData);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeaveRegion(PlayerLeaveRegionEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("region_name", event.getRegionName());
        eventData.put("leave_location", event.getLeaveLocation());
        eventData.put("world", event.getLeaveLocation().getWorld().getName());
        
        executeCustomEventScripts("onRegionLeave", event.getPlayer(), "region_leave", eventData);
        executeCustomEventScripts("onRegionLeave" + event.getRegionName(), event.getPlayer(), "region_leave_" + event.getRegionName(), eventData);
    }
    
    // ============================================================================
    // FUNCTION CALL EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFunctionCall(FunctionCallEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("function_name", event.getFunctionName());
        eventData.put("parameters", event.getParameters());
        eventData.put("parameter_count", event.getParameters().length);
        eventData.put("return_value", event.getReturnValue());
        
        executeCustomEventScripts("onFunctionCall", event.getPlayer(), "function_call", eventData);
        executeCustomEventScripts("onFunction" + event.getFunctionName(), event.getPlayer(), "function_" + event.getFunctionName(), eventData);
    }
    
    // ============================================================================
    // SCORE CHANGE EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerScoreChange(PlayerScoreChangeEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("score_type", event.getScoreType());
        eventData.put("old_score", event.getOldScore());
        eventData.put("new_score", event.getNewScore());
        eventData.put("score_change", event.getScoreChange());
        eventData.put("reason", event.getReason());
        
        executeCustomEventScripts("onScoreChange", event.getPlayer(), "score_change", eventData);
        executeCustomEventScripts("onScore" + event.getScoreType(), event.getPlayer(), "score_" + event.getScoreType(), eventData);
    }
    
    // ============================================================================
    // CUSTOM ACTION EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCustomAction(PlayerCustomActionEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("action_name", event.getActionName());
        eventData.put("action_data", event.getActionData());
        
        executeCustomEventScripts("onCustomAction", event.getPlayer(), "custom_action", eventData);
        executeCustomEventScripts("onCustom" + event.getActionName(), event.getPlayer(), "custom_" + event.getActionName(), eventData);
    }
    
    // ============================================================================
    // TIMER EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTimerExpire(TimerExpireEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("timer_name", event.getTimerName());
        eventData.put("duration", event.getDuration());
        eventData.put("timer_data", event.getTimerData());
        
        executeCustomEventScripts("onTimerExpire", event.getPlayer(), "timer_expire", eventData);
        executeCustomEventScripts("onTimer" + event.getTimerName(), event.getPlayer(), "timer_" + event.getTimerName(), eventData);
    }
    
    // ============================================================================
    // WORLD MODE CHANGE EVENTS
    // ============================================================================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldModeChange(WorldModeChangeEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("world_id", event.getWorldId());
        eventData.put("old_mode", event.getOldMode());
        eventData.put("new_mode", event.getNewMode());
        
        executeCustomEventScripts("onWorldModeChange", event.getPlayer(), "world_mode_change", eventData);
        executeCustomEventScripts("onWorldMode" + event.getNewMode(), event.getPlayer(), "world_mode_" + event.getNewMode(), eventData);
    }
}