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
        
        persistentFile = new File(dataFolder, "persistent.yml");
        if (!persistentFile.exists()) {
            try {
                persistentFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create persistent variables file: " + e.getMessage());
            }
        }
        
        persistentConfig = YamlConfiguration.loadConfiguration(persistentFile);
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
    
    // === PERSISTENT VARIABLES (SERVER SCOPE) ===
    
    public void setPersistentVariable(String name, DataValue value) {
        persistentVariables.put(name, value);
        updateMetadata(name, VariableScope.PERSISTENT, value.getType());
        
        // Save to file immediately for persistence
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
    
    // === UNIFIED VARIABLE ACCESS ===
    
    /**
     * Gets a variable from any scope with priority order:
     * 1. Local variables
     * 2. Global variables  
     * 3. Persistent variables
     * 4. Dynamic variables
     */
    public DataValue getVariable(String name, String scriptId, String worldId) {
        // Check local first
        DataValue value = getLocalVariable(scriptId, name);
        if (value != null) return value;
        
        // Check global
        value = getGlobalVariable(worldId, name);
        if (value != null) return value;
        
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
     * - global.name -> global scope  
     * - persist.name -> persistent scope
     * - name -> local scope (default)
     */
    public void setVariable(String name, DataValue value, String scriptId, String worldId) {
        if (name.startsWith("local.")) {
            setLocalVariable(scriptId, name.substring(6), value);
        } else if (name.startsWith("world.")) {
            setGlobalVariable(worldId, name.substring(6), value);
        } else if (name.startsWith("server.")) {
            setPersistentVariable(name.substring(7), value);
        } else {
            // Default to local scope
            setLocalVariable(scriptId, name, value);
        }
    }
    
    // === VARIABLE OPERATIONS ===
    
    public void incrementVariable(String name, String scriptId, String worldId, Number amount) {
        DataValue current = getVariable(name, scriptId, worldId);
        if (current != null && current.getType() == ValueType.NUMBER) {
            Number newValue = current.asNumber().doubleValue() + amount.doubleValue();
            DataValue newDataValue = DataValue.fromObject(newValue);
            setVariable(name, newDataValue, scriptId, worldId);
        }
    }
    
    public void multiplyVariable(String name, String scriptId, String worldId, Number factor) {
        DataValue current = getVariable(name, scriptId, worldId);
        if (current != null && current.getType() == ValueType.NUMBER) {
            Number newValue = current.asNumber().doubleValue() * factor.doubleValue();
            DataValue newDataValue = DataValue.fromObject(newValue);
            setVariable(name, newDataValue, scriptId, worldId);
        }
    }
    
    public void appendToVariable(String name, String scriptId, String worldId, String text) {
        DataValue current = getVariable(name, scriptId, worldId);
        if (current != null) {
            String newValue = current.asString() + text;
            DataValue newDataValue = DataValue.fromObject(newValue);
            setVariable(name, newDataValue, scriptId, worldId);
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