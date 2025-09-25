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
import java.util.Map;
import java.util.HashMap; // Add this import
import java.util.concurrent.ConcurrentHashMap;

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
    // СОБЫТИЯ РЕГИОНОВ / REGION EVENTS / REGIONEN-EREIGNISSE
    // ============================================================================
    
    /**
     * Handle player enter region event
     */
    @EventHandler
    public void onPlayerEnterRegion(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // Проверить, вошел ли игрок в новый регион / Check if player entered a new region / Prüfen, ob der Spieler eine neue Region betreten hat
        if (hasPlayerEnteredRegion(event.getFrom(), event.getTo())) {
            // Выполнить скрипт входа в регион, если он существует
            // Execute enter region script if it exists
            // Regionenbetretens-Skript ausführen, falls vorhanden
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
        // Проверить, покинул ли игрок регион / Check if player left a region / Prüfen, ob der Spieler eine Region verlassen hat
        if (hasPlayerLeftRegion(event.getFrom(), event.getTo())) {
            // Выполнить скрипт выхода из региона, если он существует
            // Execute leave region script if it exists
            // Regionenverlassens-Skript ausführen, falls vorhanden
            CodeScript script = regionScripts.get("on_leave");
            if (script != null) {
                executeScript(script, player, "region_leave", "region_" + getRegionName(event.getFrom()));
            }
        }
    }
    
    // ============================================================================
    // СОБЫТИЯ ИЗМЕНЕНИЯ ПЕРЕМЕННЫХ / VARIABLE CHANGE EVENTS / VARIABLENÄNDERUNGS-EREIGNISSE
    // ============================================================================
    
    /**
     * Handle player variable change event
     */
    @EventHandler
    public void onPlayerVariableChange(com.megacreative.coding.events.CustomVariableChangeEvent event) {
        // Вызывается при изменении переменных игрока / Triggered when player variables change / Wird ausgelöst, wenn sich Spielervariablen ändern
        Player player = event.getPlayer();
        
        // Выполнить скрипт изменения переменной, если он существует
        // Execute variable change script if it exists
        // Variablenänderungs-Skript ausführen, falls vorhanden
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
    // СОБЫТИЯ ТАЙМЕРОВ / TIMER EVENTS / TIMER-EREIGNISSE
    // ============================================================================
    
    /**
     * Handle timer expire event
     */
    @EventHandler
    public void onTimerExpire(com.megacreative.coding.events.CustomTimerExpireEvent event) {
        // Вызывается при истечении таймеров / Triggered when timers expire / Wird ausgelöst, wenn Timer ablaufen
        Player player = event.getPlayer();
        
        // Выполнить скрипт истечения таймера, если он существует
        // Execute timer expire script if it exists
        // Timerablaufs-Skript ausführen, falls vorhanden
        CodeScript script = timerScripts.get("on_expire");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("timer_name", event.getTimerName());
            data.put("duration", event.getDuration());
            executeScript(script, player, "timer_expire", event.getTimerName(), data);
        }
    }
    
    // ============================================================================
    // СОБЫТИЯ ПОЛЬЗОВАТЕЛЬСКИХ ДЕЙСТВИЙ / CUSTOM ACTION EVENTS / BENUTZERDEFINIERTE AKTIONSEREIGNISSE
    // ============================================================================
    
    /**
     * Handle player custom action event
     */
    @EventHandler
    public void onPlayerCustomAction(com.megacreative.coding.events.CustomActionEvent event) {
        // Вызывается при выполнении пользовательских действий / Triggered when players perform custom actions / Wird ausgelöst, wenn Spieler benutzerdefinierte Aktionen ausführen
        Player player = event.getPlayer();
        
        // Выполнить скрипт пользовательского действия, если он существует
        // Execute custom action script if it exists
        // Benutzerdefiniertes Aktionsskript ausführen, falls vorhanden
        CodeScript script = actionScripts.get("on_action");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("action_name", event.getActionName());
            data.put("action_data", event.getActionData());
            executeScript(script, player, "custom_action", event.getActionName(), data);
        }
    }
    
    // ============================================================================
    // СОБЫТИЯ ИЗМЕНЕНИЯ ОЧКОВ / SCORE CHANGE EVENTS / PUNKTESTANDSÄNDERUNGS-EREIGNISSE
    // ============================================================================
    
    /**
     * Handle player score change event
     */
    @EventHandler
    public void onPlayerScoreChange(com.megacreative.coding.events.PlayerScoreChangeEvent event) {
        // Вызывается при изменении очков игрока / Triggered when player scores change / Wird ausgelöst, wenn sich die Punktzahl eines Spielers ändert
        Player player = event.getPlayer();
        
        // Выполнить скрипт изменения очков, если он существует
        // Execute score change script if it exists
        // Punktestandsänderungs-Skript ausführen, falls vorhanden
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
    // СОБЫТИЯ ВЫЗОВА ФУНКЦИЙ / FUNCTION CALL EVENTS / FUNKTIONSAUFRUF-EREIGNISSE
    // ============================================================================
    
    /**
     * Handle function call event
     */
    @EventHandler
    public void onFunctionCall(com.megacreative.coding.events.FunctionCallEvent event) {
        // Вызывается при вызове функций / Triggered when functions are called / Wird ausgelöst, wenn Funktionen aufgerufen werden
        Player player = event.getPlayer();
        
        // Выполнить скрипт вызова функции, если он существует
        // Execute function call script if it exists
        // Funktionsaufruf-Skript ausführen, falls vorhanden
        CodeScript script = functionScripts.get("on_call");
        if (script != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("function_name", event.getFunctionName());
            data.put("parameters", event.getParameters());
            executeScript(script, player, "function_call", event.getFunctionName(), data);
        }
    }
    
    // ============================================================================
    // СОБЫТИЯ СМЕНЫ РЕЖИМА МИРА / WORLD MODE CHANGE EVENTS / WELTENMODUS-WECHSEL-EREIGNISSE
    // ============================================================================
    
    /**
     * Handle world mode change event
     */
    @EventHandler
    public void onWorldModeChange(com.megacreative.coding.events.WorldModeChangeEvent event) {
        // Вызывается при смене режима мира / Triggered when world modes change / Wird ausgelöst, wenn sich der Weltenmodus ändert
        Player player = event.getPlayer();
        
        // Выполнить скрипт смены режима мира, если он существует
        // Execute world mode change script if it exists
        // Weltenmodus-Änderungs-Skript ausführen, falls vorhanden
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
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ / HELPER METHODS / HILFSMETHODEN
    // ============================================================================
    
    private boolean hasPlayerEnteredRegion(Location from, Location to) {
        // Упрощенная логика обнаружения региона / Simplified region detection logic / Vereinfachte Regionenerkennungslogik
        return !getRegionName(from).equals(getRegionName(to));
    }
    
    private boolean hasPlayerLeftRegion(Location from, Location to) {
        // Упрощенная логика обнаружения региона / Simplified region detection logic / Vereinfachte Regionenerkennungslogik
        return !getRegionName(from).equals(getRegionName(to));
    }
    
    private String getRegionName(Location location) {
        // Упрощенная логика именования регионов / Simplified region naming logic / Vereinfachte Regionennamenslogik
        int regionX = location.getBlockX() / 16;
        int regionZ = location.getBlockZ() / 16;
        return regionX + "_" + regionZ;
    }
    
    private void executeScript(CodeScript script, Player player, String eventType, String eventValue) {
        executeScript(script, player, eventType, eventValue, new HashMap<>());
    }
    
    private void executeScript(CodeScript script, Player player, String eventType, String eventValue, Map<String, Object> data) {
        // Выполнить скрипт с переданным контекстом / Execute the script with the provided context / Skript mit dem bereitgestellten Kontext ausführen
        ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        if (scriptEngine != null) {
            try {
                // Установить данные события как переменные игрока перед выполнением / Set event data as player variables before execution / Ereignisdaten als Spielervariablen vor der Ausführung festlegen
                com.megacreative.coding.variables.VariableManager variableManager = scriptEngine.getVariableManager();
                if (variableManager != null) {
                    // Добавить данные события в переменные игрока / Add event data to player variables / Ereignisdaten zu den Spielervariablen hinzufügen
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
                
                // Выполнить скрипт, используя правильную сигнатуру метода / Execute the script using the correct method signature / Das Skript mit der korrekten Methodensignatur ausführen
                scriptEngine.executeScript(script, player, eventType);
            } catch (Exception e) {
                plugin.getLogger().severe("Error executing custom event script: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}