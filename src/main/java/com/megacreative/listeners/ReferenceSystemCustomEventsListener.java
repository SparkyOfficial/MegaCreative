package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location; 
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.player.PlayerMoveEvent;
import java.util.Map;
import java.util.HashMap; 

/**
 * Обработчик пользовательских событий системы скриптов
 * / Custom Script System Events Handler
 * / Benutzerdefinierte Skriptsystem-Ereignishandler
 * 
 * Обработка пользовательских событий в игре:
 * - События регионов (вход, выход)
 * - Изменение переменных
 * - Таймеры
 * - Пользовательские действия
 * - Изменение очков
 * - Вызов функций
 * - Смена режима мира
 * 
 * Handles custom in-game events:
 * - Region events (enter, leave)
 * - Variable changes
 * - Timers
 * - Custom actions
 * - Score changes
 * - Function calls
 * - World mode changes
 * 
 * Verarbeitet benutzerdefinierte Spielereignisse:
 * - Regionen-Ereignisse (Betreten, Verlassen)
 * - Variablenänderungen
 * - Timer
 * - Benutzerdefinierte Aktionen
 * - Punktestandsänderungen
 * - Funktionsaufrufe
 * - Weltenmodus-Änderungen
 */
public class ReferenceSystemCustomEventsListener implements Listener {
    
    private final MegaCreative plugin;
    
    public ReferenceSystemCustomEventsListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    
    
    
    
    /**
     * Handle player enter region event
     */
    @EventHandler
    public void onPlayerEnterRegion(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (hasPlayerEnteredRegion(event.getFrom(), event.getTo())) {
            // Add proper implementation for player enter region event
            // TODO: Implement player enter region event handling
            player.sendMessage("§ePlayer entered region: " + getRegionName(event.getTo()));
        }
    }
    
    /**
     * Handle player leave region event
     */
    @EventHandler
    public void onPlayerLeaveRegion(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (hasPlayerLeftRegion(event.getFrom(), event.getTo())) {
            // Add proper implementation for player leave region event
            // TODO: Implement player leave region event handling
            player.sendMessage("§ePlayer left region: " + getRegionName(event.getFrom()));
        }
    }
    
    
    
    
    
    /**
     * Handle player variable change event
     */
    @EventHandler
    public void onPlayerVariableChange(com.megacreative.events.CustomVariableChangeEvent event) {
        
        Player player = event.getPlayer();
        
        // Add proper implementation for player variable change event
        // TODO: Implement player variable change event handling
        player.sendMessage("§eVariable changed: " + event.getVariableName() + " = " + event.getNewValue());
        
    }
    
    
    
    
    
    /**
     * Handle timer expire event
     */
    @EventHandler
    public void onTimerExpire(com.megacreative.events.CustomTimerExpireEvent event) {
        
        Player player = event.getPlayer();
        
        // Add proper implementation for timer expire event
        // TODO: Implement timer expire event handling
        player.sendMessage("§eTimer expired: " + event.getTimerName());
        
    }
    
    
    
    
    
    /**
     * Handle player custom action event
     */
    @EventHandler
    public void onPlayerCustomAction(com.megacreative.events.CustomActionEvent event) {
        
        Player player = event.getPlayer();
        
        // Add proper implementation for player custom action event
        // TODO: Implement player custom action event handling
        player.sendMessage("§eCustom action triggered: " + event.getActionName());
        
    }
    
    
    
    
    
    /**
     * Handle player score change event
     */
    @EventHandler
    public void onPlayerScoreChange(com.megacreative.events.PlayerScoreChangeEvent event) {
        
        Player player = event.getPlayer();
        
        // Add proper implementation for player score change event
        // TODO: Implement player score change event handling
        player.sendMessage("§eScore changed: " + event.getScoreType() + " = " + String.valueOf(event.getNewScore()));
        
    }
    
    
    
    
    
    /**
     * Handle function call event
     */
    @EventHandler
    public void onFunctionCall(com.megacreative.events.FunctionCallEvent event) {
        
        Player player = event.getPlayer();
        
        // Add proper implementation for function call event
        // TODO: Implement function call event handling
        player.sendMessage("§eFunction called: " + event.getFunctionName());
        
    }
    
    
    
    
    
    /**
     * Handle world mode change event
     */
    @EventHandler
    public void onWorldModeChange(com.megacreative.events.WorldModeChangeEvent event) {
        
        Player player = event.getPlayer();
        
        // Add proper implementation for world mode change event
        // TODO: Implement world mode change event handling
        player.sendMessage("§eWorld mode changed to: " + event.getNewMode());
        
    }
    
    
    
    
    
    private boolean hasPlayerEnteredRegion(Location from, Location to) {
        
        return !getRegionName(from).equals(getRegionName(to));
    }
    
    private boolean hasPlayerLeftRegion(Location from, Location to) {
        
        return !getRegionName(from).equals(getRegionName(to));
    }
    
    private String getRegionName(Location location) {
        
        int regionX = location.getBlockX() / 16;
        int regionZ = location.getBlockZ() / 16;
        return regionX + "_" + regionZ;
    }
    
    private void executeScript(CodeScript script, Player player, String eventType, String eventValue) {
        executeScript(script, player, eventType, eventValue, new HashMap<>());
    }
    
    private void executeScript(CodeScript script, Player player, String eventType, String eventValue, Map<String, Object> data) {
        
        ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        if (scriptEngine != null) {
            try {
                
                com.megacreative.coding.variables.VariableManager variableManager = scriptEngine.getVariableManager();
                if (variableManager != null) {
                    
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
                
                
                scriptEngine.executeScript(script, player, eventType);
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error executing custom event script", e);
            }
        }
    }
}