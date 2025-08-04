package com.megacreative.coding.core;

import com.megacreative.coding.blocks.Block;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Менеджер скриптов, отвечающий за выполнение и управление скриптами.
 */
public class ScriptManager {
    private final JavaPlugin plugin;
    private final ScriptEngine scriptEngine;
    private final VariableManager variableManager;
    private final EventSystem eventSystem;
    
    // Кэш загруженных скриптов
    private final Map<String, Block> scriptCache = new ConcurrentHashMap<>();
    
    // Ограничения
    private final int maxScriptsPerPlayer = 10;
    private final long scriptTimeout = 5000; // 5 секунд
    
    // Трекинг выполнения скриптов
    private final Map<UUID, List<String>> playerScripts = new ConcurrentHashMap<>();
    private final Map<String, Long> scriptStartTimes = new ConcurrentHashMap<>();
    
    public ScriptManager(JavaPlugin plugin, ScriptEngine scriptEngine, 
                        VariableManager variableManager, EventSystem eventSystem) {
        this.plugin = plugin;
        this.scriptEngine = scriptEngine;
        this.variableManager = variableManager;
        this.eventSystem = eventSystem;
        
        // Запускаем задачу для проверки зависших скриптов
        startScriptWatchdog();
    }
    
    /**
     * Запускает выполнение скрипта.
     * 
     * @param scriptId ID скрипта
     * @param rootBlock Корневой блок скрипта
     * @param player Игрок, от имени которого выполняется скрипт
     * @param event Событие, вызвавшее выполнение (может быть null)
     * @return true, если скрипт успешно запущен
     */
    public boolean executeScript(String scriptId, Block rootBlock, Player player, Event event) {
        // Проверяем лимиты
        if (!canExecuteScript(player)) {
            return false;
        }
        
        // Создаем контекст выполнения
        BlockContext context = BlockContext.builder(plugin)
                .scriptId(scriptId)
                .player(player)
                .event(event)
                .build();
        
        // Запускаем скрипт
        scriptEngine.executeScript(scriptId, rootBlock, context);
        
        // Обновляем статистику
        trackScriptStart(scriptId, player.getUniqueId());
        
        return true;
    }
    
    /**
     * Проверяет, может ли игрок запустить новый скрипт.
     */
    private boolean canExecuteScript(Player player) {
        if (player == null) return true;
        
        List<String> scripts = playerScripts.get(player.getUniqueId());
        if (scripts != null && scripts.size() >= maxScriptsPerPlayer) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Начинает отслеживание выполнения скрипта.
     */
    private void trackScriptStart(String scriptId, UUID playerId) {
        scriptStartTimes.put(scriptId, System.currentTimeMillis());
        
        if (playerId != null) {
            playerScripts.computeIfAbsent(playerId, k -> new ArrayList<>())
                        .add(scriptId);
        }
    }
    
    /**
     * Отмечает завершение выполнения скрипта.
     */
    public void onScriptComplete(String scriptId, UUID playerId) {
        scriptStartTimes.remove(scriptId);
        
        if (playerId != null) {
            playerScripts.computeIfPresent(playerId, (k, v) -> {
                v.remove(scriptId);
                return v.isEmpty() ? null : v;
            });
        }
    }
    
    /**
     * Запускает watchdog для отслеживания зависших скриптов.
     */
    private void startScriptWatchdog() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            
            // Проверяем все выполняющиеся скрипты
            for (Map.Entry<String, Long> entry : new HashMap<>(scriptStartTimes).entrySet()) {
                String scriptId = entry.getKey();
                long startTime = entry.getValue();
                
                // Если скрипт выполняется дольше лимита, останавливаем его
                if (currentTime - startTime > scriptTimeout) {
                    plugin.getLogger().warning("Превышено время выполнения скрипта: " + scriptId);
                    scriptEngine.stopScript(scriptId);
                    scriptStartTimes.remove(scriptId);
                }
            }
            
            // Очищаем пустые записи
            playerScripts.entrySet().removeIf(e -> e.getValue().isEmpty());
            
        }, 100L, 100L); // Проверяем каждые 100 тиков (5 секунд)
    }
    
    /**
     * Останавливает все выполняющиеся скрипты.
     */
    public void stopAllScripts() {
        scriptEngine.stopAllScripts();
        scriptStartTimes.clear();
        playerScripts.clear();
    }
    
    /**
     * Получает статистику по выполняемым скриптам.
     */
    public Map<String, Object> getScriptStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Общая статистика
        stats.put("totalRunningScripts", scriptStartTimes.size());
        stats.put("playersWithScripts", playerScripts.size());
        
        // Статистика по игрокам
        Map<String, Integer> playerStats = new HashMap<>();
        playerScripts.forEach((playerId, scripts) -> {
            playerStats.put(playerId.toString(), scripts.size());
        });
        stats.put("scriptsPerPlayer", playerStats);
        
        // Время выполнения скриптов
        if (!scriptStartTimes.isEmpty()) {
            long minTime = scriptStartTimes.values().stream().min(Long::compare).orElse(0L);
            long maxTime = scriptStartTimes.values().stream().max(Long::compare).orElse(0L);
            long avgTime = (long) scriptStartTimes.values().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            
            Map<String, Long> timeStats = new HashMap<>();
            timeStats.put("minExecutionTime", System.currentTimeMillis() - maxTime);
            timeStats.put("maxExecutionTime", System.currentTimeMillis() - minTime);
            timeStats.put("avgExecutionTime", (long) avgTime);
            
            stats.put("executionTimes", timeStats);
        }
        
        return stats;
    }
}
