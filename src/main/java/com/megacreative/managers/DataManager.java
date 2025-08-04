package com.megacreative.managers;

import com.megacreative.MegaCreative;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер для управления переменными разных типов:
 * - Локальные (временные, только во время выполнения скрипта)
 * - Глобальные игрока (сохраняются между сессиями)
 * - Серверные (общие для всего сервера/мира)
 */
public class DataManager {
    
    private final MegaCreative plugin;
    
    // Локальные переменные (ExecutionContext)
    private final Map<UUID, Map<String, Object>> localVariables = new ConcurrentHashMap<>();
    
    // Глобальные переменные игроков (сохраняются между сессиями)
    private final Map<UUID, Map<String, Object>> playerVariables = new ConcurrentHashMap<>();
    
    // Серверные переменные (общие для всего сервера)
    private final Map<String, Object> serverVariables = new ConcurrentHashMap<>();
    
    public DataManager(MegaCreative plugin) {
        this.plugin = plugin;
        loadServerVariables();
    }
    
    // ========== ЛОКАЛЬНЫЕ ПЕРЕМЕННЫЕ ==========
    
    /**
     * Получить локальную переменную игрока
     */
    public Object getLocalVariable(UUID playerId, String key) {
        Map<String, Object> playerVars = localVariables.get(playerId);
        return playerVars != null ? playerVars.get(key) : null;
    }
    
    /**
     * Установить локальную переменную игрока
     */
    public void setLocalVariable(UUID playerId, String key, Object value) {
        localVariables.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(key, value);
    }
    
    /**
     * Очистить локальные переменные игрока
     */
    public void clearLocalVariables(UUID playerId) {
        localVariables.remove(playerId);
    }
    
    // ========== ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ ИГРОКА ==========
    
    /**
     * Получить глобальную переменную игрока
     */
    public Object getPlayerVariable(UUID playerId, String key) {
        Map<String, Object> playerVars = playerVariables.get(playerId);
        return playerVars != null ? playerVars.get(key) : null;
    }
    
    /**
     * Установить глобальную переменную игрока
     */
    public void setPlayerVariable(UUID playerId, String key, Object value) {
        playerVariables.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(key, value);
    }
    
    /**
     * Увеличить числовую переменную игрока
     */
    public void incrementPlayerVariable(UUID playerId, String key, double amount) {
        Object current = getPlayerVariable(playerId, key);
        double currentValue = 0.0;
        
        if (current != null) {
            try {
                currentValue = Double.parseDouble(current.toString());
            } catch (NumberFormatException e) {
                // Если не число, начинаем с 0
            }
        }
        
        setPlayerVariable(playerId, key, currentValue + amount);
    }
    
    // ========== СЕРВЕРНЫЕ ПЕРЕМЕННЫЕ ==========
    
    /**
     * Получить серверную переменную
     */
    public Object getServerVariable(String key) {
        return serverVariables.get(key);
    }
    
    /**
     * Установить серверную переменную
     */
    public void setServerVariable(String key, Object value) {
        serverVariables.put(key, value);
        saveServerVariables();
    }
    
    /**
     * Увеличить числовую серверную переменную
     */
    public void incrementServerVariable(String key, double amount) {
        Object current = getServerVariable(key);
        double currentValue = 0.0;
        
        if (current != null) {
            try {
                currentValue = Double.parseDouble(current.toString());
            } catch (NumberFormatException e) {
                // Если не число, начинаем с 0
            }
        }
        
        setServerVariable(key, currentValue + amount);
    }
    
    // ========== СОХРАНЕНИЕ И ЗАГРУЗКА ==========
    
    /**
     * Загрузить данные игрока при входе
     */
    public void loadPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        File playerFile = new File(plugin.getDataFolder(), "players/" + playerId + ".yml");
        
        if (playerFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            Map<String, Object> vars = new ConcurrentHashMap<>();
            
            if (config.contains("variables")) {
                for (String key : config.getConfigurationSection("variables").getKeys(false)) {
                    vars.put(key, config.get("variables." + key));
                }
            }
            
            playerVariables.put(playerId, vars);
            plugin.getLogger().info("Загружены данные игрока: " + player.getName() + " (" + vars.size() + " переменных)");
        }
    }
    
    /**
     * Сохранить данные игрока при выходе
     */
    public void savePlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        Map<String, Object> vars = playerVariables.get(playerId);
        
        if (vars != null && !vars.isEmpty()) {
            File playerFile = new File(plugin.getDataFolder(), "players/" + playerId + ".yml");
            playerFile.getParentFile().mkdirs();
            
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                config.set("variables." + entry.getKey(), entry.getValue());
            }
            
            try {
                config.save(playerFile);
                plugin.getLogger().info("Сохранены данные игрока: " + player.getName() + " (" + vars.size() + " переменных)");
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка сохранения данных игрока " + player.getName() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Загрузить серверные переменные
     */
    private void loadServerVariables() {
        File serverFile = new File(plugin.getDataFolder(), "server_data.yml");
        
        if (serverFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(serverFile);
            
            if (config.contains("variables")) {
                for (String key : config.getConfigurationSection("variables").getKeys(false)) {
                    serverVariables.put(key, config.get("variables." + key));
                }
            }
            
            plugin.getLogger().info("Загружены серверные переменные: " + serverVariables.size() + " переменных");
        }
    }
    
    /**
     * Сохранить серверные переменные
     */
    private void saveServerVariables() {
        File serverFile = new File(plugin.getDataFolder(), "server_data.yml");
        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<String, Object> entry : serverVariables.entrySet()) {
            config.set("variables." + entry.getKey(), entry.getValue());
        }
        
        try {
            config.save(serverFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения серверных переменных: " + e.getMessage());
        }
    }
    
    /**
     * Сохранить все данные (вызывается при выключении сервера)
     */
    public void saveAllData() {
        // Сохраняем данные всех онлайн игроков
        plugin.getServer().getOnlinePlayers().forEach(this::savePlayerData);
        // Сохраняем серверные переменные
        saveServerVariables();
        plugin.getLogger().info("Все данные игроков и сервера сохранены.");
    }

    /**
     * Корректное завершение работы менеджера данных.
     */
    public void shutdown() {
        plugin.getLogger().info("Начало сохранения всех данных...");
        saveAllData();
    }
}