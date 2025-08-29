package com.megacreative.coding.variables;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Advanced variable management system with support for:
 * - Local variables (script scope)
 * - Global variables (world scope) 
 * - Persistent variables (server scope)
 * - Dynamic variables (computed values)
 * - Type-safe operations
 * - Real-time synchronization
 */
public class VariableManager {
    
    private final MegaCreative plugin;
    
    // Variable storage by scope
    private final Map<String, Map<String, DataValue>> localVariables = new ConcurrentHashMap<>();
    private final Map<String, Map<String, DataValue>> globalVariables = new ConcurrentHashMap<>();
    private final Map<String, DataValue> persistentVariables = new ConcurrentHashMap<>();
    private final Map<String, DynamicVariable> dynamicVariables = new ConcurrentHashMap<>();
    
    // Player-specific variables (from DataManager functionality)
    private final Map<UUID, Map<String, DataValue>> playerVariables = new ConcurrentHashMap<>();
    
    // Server variables storage
    private final Map<String, DataValue> serverVariables = new ConcurrentHashMap<>();
    private File serverVarsFile;
    
    // Variable metadata
    private final Map<String, VariableMetadata> variableMetadata = new ConcurrentHashMap<>();
    
    // File storage
    private File persistentFile;
    private YamlConfiguration persistentConfig;
    
    public VariableManager(MegaCreative plugin) {
        this.plugin = plugin;
        initializeStorage();
        loadPersistentVariables();
        registerDynamicVariables();
    }
    
    private void initializeStorage() {
        File dataFolder = new File(plugin.getDataFolder(), "variables");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        // Initialize persistent variables file
        persistentFile = new File(dataFolder, "persistent.yml");
        if (!persistentFile.exists()) {
            try {
                persistentFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create persistent variables file: " + e.getMessage());
            }
        }
        persistentConfig = YamlConfiguration.loadConfiguration(persistentFile);
        
        // Initialize server variables file
        serverVarsFile = new File(dataFolder, "server_vars.yml");
        if (!serverVarsFile.exists()) {
            try {
                serverVarsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create server variables file: " + e.getMessage());
            }
        }
    }
    
    // === LOCAL VARIABLES (SCRIPT SCOPE) ===
    
    public void setLocalVariable(String scriptId, String name, DataValue value) {
        localVariables.computeIfAbsent(scriptId, k -> new ConcurrentHashMap<>()).put(name, value);
        updateMetadata(name, VariableScope.LOCAL, value.getType());
        
        plugin.getLogger().fine("Set local variable: " + scriptId + "." + name + " = " + value.asString());
    }
    
    public DataValue getLocalVariable(String scriptId, String name) {
        Map<String, DataValue> scriptVars = localVariables.get(scriptId);
        return scriptVars != null ? scriptVars.get(name) : null;
    }
    
    public void clearLocalVariables(String scriptId) {
        localVariables.remove(scriptId);
        plugin.getLogger().fine("Cleared local variables for script: " + scriptId);
    }
    
    // === GLOBAL VARIABLES (WORLD SCOPE) ===
    
    public void setGlobalVariable(String worldId, String name, DataValue value) {
        globalVariables.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>()).put(name, value);
        updateMetadata(name, VariableScope.WORLD, value.getType());
        
        // Notify all players in world about variable change
        notifyPlayersInWorld(worldId, name, value);
        
        plugin.getLogger().info("Set global variable: " + worldId + "." + name + " = " + value.asString());
    }
    
    public DataValue getGlobalVariable(String worldId, String name) {
        Map<String, DataValue> worldVars = globalVariables.get(worldId);
        return worldVars != null ? worldVars.get(name) : null;
    }
    
    public Map<String, DataValue> getAllGlobalVariables(String worldId) {
        return new HashMap<>(globalVariables.getOrDefault(worldId, new HashMap<>()));
    }
    
    // === PLAYER VARIABLES ===
    
    public void setPlayerVariable(UUID playerId, String name, DataValue value) {
        playerVariables.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(name, value);
        updateMetadata("player." + name, VariableScope.PLAYER, value.getType());
        savePlayerVariables(playerId);
    }
    
    public DataValue getPlayerVariable(UUID playerId, String name) {
        Map<String, DataValue> vars = playerVariables.get(playerId);
        return vars != null ? vars.get(name) : null;
    }
    
    public void incrementPlayerVariable(UUID playerId, String name, double amount) {
        DataValue current = getPlayerVariable(playerId, name);
        double currentValue = 0.0;
        
        if (current != null && current.getType() == ValueType.NUMBER) {
            currentValue = current.asNumber().doubleValue();
        }
        
        setPlayerVariable(playerId, name, DataValue.of(currentValue + amount));
    }
    
    public void savePlayerVariables(UUID playerId) {
        // Implementation to save player variables to file
        // This will be called periodically and on server shutdown
    }
    
    public void loadPlayerVariables(UUID playerId) {
        // Implementation to load player variables from file
    }
    
    // === SERVER VARIABLES ===
    
    public void setServerVariable(String name, DataValue value) {
        serverVariables.put(name, value);
        updateMetadata("server." + name, VariableScope.SERVER, value.getType());
        saveServerVariables();
    }
    
    public DataValue getServerVariable(String name) {
        return serverVariables.get(name);
    }
    
    public void saveServerVariables() {
        // Implementation to save server variables to file
        // This will be called when variables change and on server shutdown
    }
    
    public void loadServerVariables() {
        // Implementation to load server variables from file
    }
    
    // === PERSISTENT VARIABLES (SERVER SCOPE) ===
    
    public void setPersistentVariable(String name, DataValue value) {
        persistentVariables.put(name, value);
        updateMetadata(name, VariableScope.SERVER, value.getType());
        savePersistentVariable(name, value);
        
        plugin.getLogger().info("Set persistent variable: " + name + " = " + value.asString());
    }
    
    public DataValue getPersistentVariable(String name) {
        return persistentVariables.get(name);
    }
    
    private void savePersistentVariable(String name, DataValue value) {
        persistentConfig.set("variables." + name, value.serialize());
        try {
            persistentConfig.save(persistentFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save persistent variable " + name + ": " + e.getMessage());
        }
    }
    
    private void loadPersistentVariables() {
        if (!persistentConfig.contains("variables")) return;
        
        for (String name : persistentConfig.getConfigurationSection("variables").getKeys(false)) {
            try {
                Map<String, Object> serialized = persistentConfig.getConfigurationSection("variables." + name).getValues(false);
                DataValue value = DataValue.deserialize(serialized);
                persistentVariables.put(name, value);
               updateMetadata(name, VariableScope.SERVER, value.getType());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load persistent variable " + name + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + persistentVariables.size() + " persistent variables");
    }
    
    // === DYNAMIC VARIABLES (COMPUTED VALUES) ===
    
    private void registerDynamicVariables() {
        // Player-related dynamic variables
        registerDynamicVariable("player_count", () -> plugin.getServer().getOnlinePlayers().size());
        registerDynamicVariable("max_players", () -> plugin.getServer().getMaxPlayers());
        registerDynamicVariable("server_time", () -> System.currentTimeMillis());
        registerDynamicVariable("server_uptime", () -> System.currentTimeMillis() - getServerStartTime());
        
        // World-related dynamic variables
        registerDynamicVariable("total_worlds", () -> plugin.getServer().getWorlds().size());
        
        // Memory-related dynamic variables
        registerDynamicVariable("used_memory", () -> {
            Runtime runtime = Runtime.getRuntime();
            return (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024; // MB
        });
        
        registerDynamicVariable("free_memory", () -> {
            Runtime runtime = Runtime.getRuntime();
            return runtime.freeMemory() / 1024 / 1024; // MB
        });
        
        plugin.getLogger().info("Registered " + dynamicVariables.size() + " dynamic variables");
    }
    
    public void registerDynamicVariable(String name, DynamicVariable.ValueSupplier supplier) {
        dynamicVariables.put(name, new DynamicVariable(name, supplier));
       updateMetadata(name, VariableScope.LOCAL, ValueType.ANY);
    }
    
    public DataValue getDynamicVariable(String name) {
        DynamicVariable dynamic = dynamicVariables.get(name);
        if (dynamic != null) {
            return DataValue.fromObject(dynamic.getValue());
        }
        return null;
    }
    
    // === PLAYER VARIABLES (MIGRATED FROM DATAMANAGER) ===
    
    /**
     * Get player-specific variable
     */
    public DataValue getPlayerVariable(UUID playerId, String name) {
        Map<String, DataValue> playerVars = playerVariables.get(playerId);
        return playerVars != null ? playerVars.get(name) : null;
    }
    
    /**
     * Set player-specific variable
     */
    public void setPlayerVariable(UUID playerId, String name, DataValue value) {
        playerVariables.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(name, value);
        updateMetadata(name, VariableScope.PLAYER, value.getType());
        plugin.getLogger().fine("Set player variable: " + playerId + "." + name + " = " + value.asString());
    }
    
    /**
     * Convenience method for setting player variable from Object (DataManager compatibility)
     */
    public void setPlayerVariable(UUID playerId, String name, Object value) {
        setPlayerVariable(playerId, name, DataValue.fromObject(value));
    }
    
    /**
     * Get player variable as Object (DataManager compatibility)
     */
    public Object getPlayerVariable(UUID playerId, String name, Object defaultValue) {
        DataValue value = getPlayerVariable(playerId, name);
        return value != null ? value.getValue() : defaultValue;
    }
    
    /**
     * Increment numeric player variable
     */
    public void incrementPlayerVariable(UUID playerId, String name, double amount) {
        DataValue current = getPlayerVariable(playerId, name);
        double currentValue = 0.0;
        
        if (current != null && current.getType() == ValueType.NUMBER) {
            currentValue = current.asNumber().doubleValue();
        }
        
        setPlayerVariable(playerId, name, DataValue.fromObject(currentValue + amount));
    }
    
    /**
     * Load player data from file (migrated from DataManager)
     */
    public void loadPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        File playerFile = new File(plugin.getDataFolder(), "players/" + playerId + ".yml");
        
        if (playerFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            Map<String, DataValue> vars = new ConcurrentHashMap<>();
            
            if (config.contains("variables")) {
                for (String key : config.getConfigurationSection("variables").getKeys(false)) {
                    try {
                        Object rawValue = config.get("variables." + key);
                        DataValue value = DataValue.fromObject(rawValue);
                        vars.put(key, value);
                        updateMetadata(key, VariableScope.PLAYER, value.getType());
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load player variable " + key + " for " + player.getName() + ": " + e.getMessage());
                    }
                }
            }
            
            playerVariables.put(playerId, vars);
            plugin.getLogger().info("Loaded player data: " + player.getName() + " (" + vars.size() + " variables)");
        }
    }
    
    /**
     * Get all player variables as a map
     */
    public Map<String, DataValue> getPlayerVariables(UUID playerId) {
        Map<String, DataValue> vars = playerVariables.get(playerId);
        return vars != null ? new ConcurrentHashMap<>(vars) : new ConcurrentHashMap<>();
    }
    
    /**
     * Get all local variables for a script
     */
    public Map<String, DataValue> getLocalVariables(String scriptId) {
        Map<String, DataValue> vars = localVariables.get(scriptId);
        return vars != null ? new ConcurrentHashMap<>(vars) : new ConcurrentHashMap<>();
    }
    
    /**
     * Get all global variables for a world
     */
    public Map<String, DataValue> getGlobalVariables(String worldId) {
        Map<String, DataValue> vars = globalVariables.get(worldId);
        return vars != null ? new ConcurrentHashMap<>(vars) : new ConcurrentHashMap<>();
    }
    
    /**
     * Get all server variables
     */
    public Map<String, DataValue> getServerVariables() {
        return new ConcurrentHashMap<>(serverVariables);
    }
    
    /**
     * Get all persistent variables
     */
    public Map<String, DataValue> getPersistentVariables() {
        return new ConcurrentHashMap<>(persistentVariables);
    }
    
    /**
     * Remove a player variable
     */
    public void removePlayerVariable(UUID playerId, String name) {
        Map<String, DataValue> playerVars = playerVariables.get(playerId);
        if (playerVars != null) {
            playerVars.remove(name);
            savePlayerVariables(playerId);
        }
    }
    
    /**
     * Remove a local variable
     */
    public void removeLocalVariable(String scriptId, String name) {
        Map<String, DataValue> scriptVars = localVariables.get(scriptId);
        if (scriptVars != null) {
            scriptVars.remove(name);
        }
    }
    
    /**
     * Remove a global variable
     */
    public void removeGlobalVariable(String worldId, String name) {
        Map<String, DataValue> worldVars = globalVariables.get(worldId);
        if (worldVars != null) {
            worldVars.remove(name);
            // Notify players about variable removal
            notifyPlayersInWorld(worldId, name, null);
        }
    }
    
    /**
     * Remove a server variable
     */
    public void removeServerVariable(String name) {
        serverVariables.remove(name);
        saveServerVariables();
    }
    
    /**
     * Remove a persistent variable
     */
    public void removePersistentVariable(String name) {
        persistentVariables.remove(name);
        persistentConfig.set("variables." + name, null);
        try {
            persistentConfig.save(persistentFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to remove persistent variable " + name + ": " + e.getMessage());
        }
    }
    
    /**
     * Save player data to file (migrated from DataManager)
     */
    public void savePlayerData(Player player) {
        savePlayerVariables(player.getUniqueId());
    }
    
    /**
     * Save player variables to file
     */
    public void savePlayerVariables(UUID playerId) {
        Map<String, DataValue> vars = playerVariables.get(playerId);
        if (vars == null || vars.isEmpty()) {
            return;
        }
        
        File playerFile = new File(plugin.getDataFolder(), "players/" + playerId + ".yml");
        playerFile.getParentFile().mkdirs();
        
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, DataValue> entry : vars.entrySet()) {
            config.set("variables." + entry.getKey(), entry.getValue().getValue());
        }
        
        try {
            config.save(playerFile);
            plugin.getLogger().fine("Saved player variables: " + playerId + " (" + vars.size() + " variables)");
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving player variables for " + playerId + ": " + e.getMessage());
        }
    }
    
    /**
     * Save all modified player variables to disk
     */
    public void saveAllPlayerVariables() {
        for (UUID playerId : playerVariables.keySet()) {
            savePlayerVariables(playerId);
        }
    }
    
    /**
     * Save server variables to file
     */
    public void saveServerVariables() {
        try {
            File dataFolder = new File(plugin.getDataFolder(), "variables");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, DataValue> entry : serverVariables.entrySet()) {
                config.set(entry.getKey(), entry.getValue().getValue());
            }
            
            config.save(serverVarsFile);
            plugin.getLogger().fine("Saved " + serverVariables.size() + " server variables");
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving server variables: " + e.getMessage());
        }
    }
    
    /**
     * Load server variables from file
     */
    public void loadServerVariables() {
        if (!serverVarsFile.exists()) {
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(serverVarsFile);
        for (String key : config.getKeys(true)) {
            try {
                Object value = config.get(key);
                if (value != null) {
                    serverVariables.put(key, DataValue.fromObject(value));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load server variable " + key + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + serverVariables.size() + " server variables");
    }
    
    /**
     * Get server variable as Object (DataManager compatibility)
     */
    public Object getServerVariable(String name) {
        DataValue value = getPersistentVariable(name);
        return value != null ? value.getValue() : null;
    }
    
    /**
     * Set server variable from Object (DataManager compatibility)
     */
    public void setServerVariable(String name, Object value) {
        setPersistentVariable(name, DataValue.fromObject(value));
    }
    
    /**
     * Increment numeric server variable
     */
    public void incrementServerVariable(String name, double amount) {
        DataValue current = getPersistentVariable(name);
        double currentValue = 0.0;
        
        if (current != null && current.getType() == ValueType.NUMBER) {
            currentValue = current.asNumber().doubleValue();
        }
        
        setPersistentVariable(name, DataValue.fromObject(currentValue + amount));
    }
    
    // === UNIFIED VARIABLE ACCESS ===
    
    /**
     * Gets a variable from any scope with priority order:
     * 1. Local variables
     * 2. Global variables  
     * 3. Player variables (if playerId provided)
     * 4. Persistent variables
     * 5. Dynamic variables
     */
    public DataValue getVariable(String name, String scriptId, String worldId) {
        return getVariable(name, scriptId, worldId, null);
    }
    
    /**
     * Gets a variable from any scope with priority order (with optional player context)
     */
    public DataValue getVariable(String name, String scriptId, String worldId, UUID playerId) {
        // Check local first
        DataValue value = getLocalVariable(scriptId, name);
        if (value != null) return value;
        
        // Check global
        value = getGlobalVariable(worldId, name);
        if (value != null) return value;
        
        // Check player variables if player context provided
        if (playerId != null) {
            value = getPlayerVariable(playerId, name);
            if (value != null) return value;
        }
        
        // Check persistent
        value = getPersistentVariable(name);
        if (value != null) return value;
        
        // Check dynamic
        value = getDynamicVariable(name);
        if (value != null) return value;
        
        return null;
    }
    
    /**
     * Sets a variable in the appropriate scope based on prefix
     * - local.name -> local scope
     * - world.name -> global scope  
     * - player.name -> player scope
     * - server.name -> persistent scope
     * - name -> local scope (default)
     */
    public void setVariable(String name, DataValue value, String scriptId, String worldId) {
        setVariable(name, value, scriptId, worldId, null);
    }
    
    /**
     * Sets a variable in the appropriate scope (with optional player context)
     */
    public void setVariable(String name, DataValue value, String scriptId, String worldId, UUID playerId) {
        if (name.startsWith("local.")) {
            setLocalVariable(scriptId, name.substring(6), value);
        } else if (name.startsWith("world.")) {
            setGlobalVariable(worldId, name.substring(6), value);
        } else if (name.startsWith("player.") && playerId != null) {
            setPlayerVariable(playerId, name.substring(7), value);
        } else if (name.startsWith("server.")) {
            setPersistentVariable(name.substring(7), value);
        } else {
            // Default to local scope
            setLocalVariable(scriptId, name, value);
        }
    }
    
    // === VARIABLE OPERATIONS ===
    
    public void incrementVariable(String name, String scriptId, String worldId, Number amount) {
        incrementVariable(name, scriptId, worldId, null, amount);
    }
    
    public void incrementVariable(String name, String scriptId, String worldId, UUID playerId, Number amount) {
        DataValue current = getVariable(name, scriptId, worldId, playerId);
        if (current != null && current.getType() == ValueType.NUMBER) {
            Number newValue = current.asNumber().doubleValue() + amount.doubleValue();
            DataValue newDataValue = DataValue.fromObject(newValue);
            setVariable(name, newDataValue, scriptId, worldId, playerId);
        }
    }
    
    public void multiplyVariable(String name, String scriptId, String worldId, Number factor) {
        multiplyVariable(name, scriptId, worldId, null, factor);
    }
    
    public void multiplyVariable(String name, String scriptId, String worldId, UUID playerId, Number factor) {
        DataValue current = getVariable(name, scriptId, worldId, playerId);
        if (current != null && current.getType() == ValueType.NUMBER) {
            Number newValue = current.asNumber().doubleValue() * factor.doubleValue();
            DataValue newDataValue = DataValue.fromObject(newValue);
            setVariable(name, newDataValue, scriptId, worldId, playerId);
        }
    }
    
    public void appendToVariable(String name, String scriptId, String worldId, String text) {
        appendToVariable(name, scriptId, worldId, null, text);
    }
    
    public void appendToVariable(String name, String scriptId, String worldId, UUID playerId, String text) {
        DataValue current = getVariable(name, scriptId, worldId, playerId);
        if (current != null) {
            String newValue = current.asString() + text;
            DataValue newDataValue = DataValue.fromObject(newValue);
            setVariable(name, newDataValue, scriptId, worldId, playerId);
        }
    }
    
    // === METADATA AND INTROSPECTION ===
    
    private void updateMetadata(String name, VariableScope scope, ValueType type) {
        variableMetadata.put(name, new VariableMetadata(name, scope, type, System.currentTimeMillis()));
    }
    
    public VariableMetadata getVariableMetadata(String name) {
        return variableMetadata.get(name);
    }
    
    public Set<String> getAllVariableNames() {
        Set<String> names = new HashSet<>();
        localVariables.values().forEach(map -> names.addAll(map.keySet()));
        globalVariables.values().forEach(map -> names.addAll(map.keySet()));
        playerVariables.values().forEach(map -> names.addAll(map.keySet()));
        names.addAll(persistentVariables.keySet());
        names.addAll(dynamicVariables.keySet());
        return names;
    }
    
    public List<String> getVariablesByScope(VariableScope scope) {
        return variableMetadata.values().stream()
                .filter(meta -> meta.getScope() == scope)
                .map(VariableMetadata::getName)
                .sorted()
                .toList();
    }
    
    // === UTILITIES ===
    
    private void notifyPlayersInWorld(String worldId, String name, DataValue value) {
        // Find all players in the world and send them a variable update notification
        plugin.getServer().getOnlinePlayers().stream()
                .filter(player -> {
                    var world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
                    return world != null && world.getId().equals(worldId);
                })
                .forEach(player -> {
                    if (player.hasPermission("megacreative.debug")) {
                        player.sendMessage("§7[Variable] §e" + name + " §7= §f" + value.asString());
                    }
                });
    }
    
    public void cleanup() {
        // Save all persistent variables
        for (Map.Entry<String, DataValue> entry : persistentVariables.entrySet()) {
            savePersistentVariable(entry.getKey(), entry.getValue());
        }
        
        // Clear memory
        localVariables.clear();
        globalVariables.clear();
        playerVariables.clear();
        dynamicVariables.clear();
        variableMetadata.clear();
        
        plugin.getLogger().info("Variable manager cleaned up");
    }
    
    private long getServerStartTime() {
        // Fallback implementation since getStartTime() doesn't exist
        return System.currentTimeMillis() - 60000; // Assume 1 minute uptime as fallback
    }
}

/**
 * Variable metadata for introspection
 */
class VariableMetadata {
    private final String name;
    private final VariableScope scope;
    private final ValueType type;
    private final long createdTime;
    private long lastModified;
    
    public VariableMetadata(String name, VariableScope scope, ValueType type, long createdTime) {
        this.name = name;
        this.scope = scope;
        this.type = type;
        this.createdTime = createdTime;
        this.lastModified = createdTime;
    }
    
    public String getName() { return name; }
    public VariableScope getScope() { return scope; }
    public ValueType getType() { return type; }
    public long getCreatedTime() { return createdTime; }
    public long getLastModified() { return lastModified; }
    
    public void updateLastModified() {
        this.lastModified = System.currentTimeMillis();
    }
}

/**
 * Dynamic variable implementation
 */
class DynamicVariable {
    @FunctionalInterface
    public interface ValueSupplier {
        Object get();
    }
    
    private final String name;
    private final ValueSupplier supplier;
    
    public DynamicVariable(String name, ValueSupplier supplier) {
        this.name = name;
        this.supplier = supplier;
    }
    
    public String getName() { return name; }
    
    public Object getValue() {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }
}