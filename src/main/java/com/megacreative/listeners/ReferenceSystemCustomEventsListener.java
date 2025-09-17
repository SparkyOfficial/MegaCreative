package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location; // Add this import
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
// Replace custom events with standard Bukkit events
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.Map;
import java.util.HashMap; // Add this import
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎆 Reference System Custom Events Listener
 * 
 * Comprehensive custom event coverage with reference system-style functionality:
 * - Region events (enter, leave)
 * - Variable change events
 * - Timer events
 * - Custom action events
 * - Score change events
 * - Function call events
 * - World mode change events
 */
public class ReferenceSystemCustomEventsListener implements Listener {
    
    private final MegaCreative plugin;
    private final Map<String, CodeScript> regionScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> variableScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> timerScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> actionScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> scoreScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> functionScripts = new ConcurrentHashMap<>();
    private final Map<String, CodeScript> worldScripts = new ConcurrentHashMap<>();
    
    public ReferenceSystemCustomEventsListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    // ============================================================================
    // REGION EVENTS
    // ============================================================================
    
    /**
     * Handle player enter region event
     */
    @EventHandler
    public void onPlayerEnterRegion(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // Check if player entered a new region
        if (hasPlayerEnteredRegion(event.getFrom(), event.getTo())) {
            plugin.getLogger().fine("🎆 Player entered region: " + player.getName());
            
            // Execute enter region script if exists
            CodeScript script = regionScripts.get("on_enter");
            if (script != null) {
                executeScript(script, player, "region_enter", "region_" + getRegionName(event.getTo()));
            }
        }
    }
    
    /**
     * Handle player leave region event
     */
    @EventHandler
    public void onPlayerLeaveRegion(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // Check if player left a region
        if (hasPlayerLeftRegion(event.getFrom(), event.getTo())) {
            plugin.getLogger().fine("🎆 Player left region: " + player.getName());
            
            // Execute leave region script if exists
            CodeScript script = regionScripts.get("on_leave");
            if (script != null) {
                executeScript(script, player, "region_leave", "region_" + getRegionName(event.getFrom()));
            }
        }
    }
    
    // ============================================================================
    // VARIABLE CHANGE EVENTS
    // ============================================================================
    
    /**
     * Handle player variable change event
     */
    @EventHandler
    public void onPlayerVariableChange(com.megacreative.coding.events.CustomVariableChangeEvent event) {
        // Triggered when player variables change
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT Player variable changed: " + player.getName() + " - " + event.getVariableName());
        
        // Execute variable change script if exists
        CodeScript script = variableScripts.get("on_change");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("variable_name", event.getVariableName());
            data.put("old_value", event.getOldValue());
            data.put("new_value", event.getNewValue());
            executeScript(script, player, "variable_change", event.getVariableName(), data);
        }
    }
    
    // ============================================================================
    // TIMER EVENTS
    // ============================================================================
    
    /**
     * Handle timer expire event
     */
    @EventHandler
    public void onTimerExpire(com.megacreative.coding.events.CustomTimerExpireEvent event) {
        // Triggered when timers expire
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT Timer expired: " + event.getTimerName());
        
        // Execute timer expire script if exists
        CodeScript script = timerScripts.get("on_expire");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("timer_name", event.getTimerName());
            data.put("duration", event.getDuration());
            executeScript(script, player, "timer_expire", event.getTimerName(), data);
        }
    }
    
    // ============================================================================
    // CUSTOM ACTION EVENTS
    // ============================================================================
    
    /**
     * Handle player custom action event
     */
    @EventHandler
    public void onPlayerCustomAction(com.megacreative.coding.events.CustomActionEvent event) {
        // Triggered when players perform custom actions
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT Player performed custom action: " + player.getName() + " - " + event.getActionName());
        
        // Execute custom action script if exists
        CodeScript script = actionScripts.get("on_action");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("action_name", event.getActionName());
            data.put("action_data", event.getActionData());
            executeScript(script, player, "custom_action", event.getActionName(), data);
        }
    }
    
    // ============================================================================
    // SCORE CHANGE EVENTS
    // ============================================================================
    
    /**
     * Handle player score change event
     */
    @EventHandler
    public void onPlayerScoreChange(com.megacreative.coding.events.PlayerScoreChangeEvent event) {
        // Triggered when player scores change
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT Player score changed: " + player.getName() + " - " + event.getScoreType());
        
        // Execute score change script if exists
        CodeScript script = scoreScripts.get("on_change");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("score_type", event.getScoreType());
            data.put("old_score", event.getOldScore());
            data.put("new_score", event.getNewScore());
            executeScript(script, player, "score_change", event.getScoreType(), data);
        }
    }
    
    // ============================================================================
    // FUNCTION CALL EVENTS
    // ============================================================================
    
    /**
     * Handle function call event
     */
    @EventHandler
    public void onFunctionCall(com.megacreative.coding.events.FunctionCallEvent event) {
        // Triggered when functions are called
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT Function called: " + event.getFunctionName());
        
        // Execute function call script if exists
        CodeScript script = functionScripts.get("on_call");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("function_name", event.getFunctionName());
            data.put("parameters", event.getParameters());
            executeScript(script, player, "function_call", event.getFunctionName(), data);
        }
    }
    
    // ============================================================================
    // WORLD MODE CHANGE EVENTS
    // ============================================================================
    
    /**
     * Handle world mode change event
     */
    @EventHandler
    public void onWorldModeChange(com.megacreative.coding.events.WorldModeChangeEvent event) {
        // Triggered when world modes change
        Player player = event.getPlayer();
        plugin.getLogger().fine(".EVT World mode changed: " + event.getWorldName() + " - " + event.getNewMode());
        
        // Execute world mode change script if exists
        CodeScript script = worldScripts.get("on_mode_change");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("world_name", event.getWorldName());
            data.put("old_mode", event.getOldMode());
            data.put("new_mode", event.getNewMode());
            executeScript(script, player, "world_mode_change", event.getWorldName(), data);
        }
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private boolean hasPlayerEnteredRegion(Location from, Location to) {
        // Simplified region detection logic
        return !getRegionName(from).equals(getRegionName(to));
    }
    
    private boolean hasPlayerLeftRegion(Location from, Location to) {
        // Simplified region detection logic
        return !getRegionName(from).equals(getRegionName(to));
    }
    
    private String getRegionName(Location location) {
        // Simplified region naming logic
        int regionX = location.getBlockX() / 16;
        int regionZ = location.getBlockZ() / 16;
        return regionX + "_" + regionZ;
    }
    
    private void executeScript(CodeScript script, Player player, String eventType, String eventValue) {
        executeScript(script, player, eventType, eventValue, new HashMap<>());
    }
    
    private void executeScript(CodeScript script, Player player, String eventType, String eventValue, Map<String, Object> data) {
        // Execute the script with the context
        ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        if (scriptEngine != null) {
            try {
                // Set event data as player variables before execution
                com.megacreative.coding.variables.VariableManager variableManager = scriptEngine.getVariableManager();
                if (variableManager != null) {
                    // Add event data to player variables
                    variableManager.setPlayerVariable(player.getUniqueId(), "event_type", 
                        com.megacreative.coding.values.DataValue.of(eventType));
                    variableManager.setPlayerVariable(player.getUniqueId(), "event_value", 
                        com.megacreative.coding.values.DataValue.of(eventValue));
                    
                    if (data != null) {
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            variableManager.setPlayerVariable(player.getUniqueId(), entry.getKey(), 
                                com.megacreative.coding.values.DataValue.fromObject(entry.getValue()));
                        }
                    }
                }
                
                // Execute the script using the correct method signature
                scriptEngine.executeScript(script, player, eventType);
            } catch (Exception e) {
                plugin.getLogger().severe("Error executing custom event script: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}