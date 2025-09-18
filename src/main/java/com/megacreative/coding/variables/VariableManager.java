package com.megacreative.coding.variables;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Manages variables for the MegaCreative plugin.
 * Handles different scopes of variables including local, global, player, and persistent variables.
 * Enhanced with improved scope resolution and fallback mechanisms.
 */
public class VariableManager implements IVariableManager {
    
    private final MegaCreative plugin;
    private final Map<String, Map<String, DataValue>> globalVariables = new ConcurrentHashMap<>();
    private final Map<String, Map<String, DataValue>> localVariables = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, DataValue>> playerVariables = new ConcurrentHashMap<>();
    private final Map<String, DataValue> serverVariables = new ConcurrentHashMap<>();
    private final Map<String, DataValue> persistentVariables = new ConcurrentHashMap<>();
    private final Map<String, DynamicVariable> dynamicVariables = new ConcurrentHashMap<>();
    private final Map<String, VariableMetadata> variableMetadata = new ConcurrentHashMap<>();
    
    private static final String VARIABLES_FOLDER = "variables";
    private File persistentFile;
    private File serverVarsFile;
    private YamlConfiguration persistentConfig;
    
    public VariableManager(MegaCreative plugin) {
        this.plugin = plugin;
        initialize();
    }
    
    private void initialize() {
        initializeStorage();
        loadPersistentData();
        registerDynamicVariables();
    }
    
    // === STORAGE INITIALIZATION ===
    
    @Override
    public void setVariable(String name, DataValue value, VariableScope scope, String context) {
        if (name == null || value == null || scope == null) {
            throw new IllegalArgumentException("Name, value, and scope cannot be null");
        }
        
        switch (scope) {
            case LOCAL:
                setLocalVariable(context, name, value);
                break;
            case GLOBAL:
                setGlobalVariable(name, value);
                break;
            case PLAYER:
                try {
                    UUID playerId = UUID.fromString(context);
                    setPlayerVariable(playerId, name, value);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "Invalid player UUID: " + context, e);
                }
                break;
            case SERVER:
                setServerVariable(name, value);
                break;
            case PERSISTENT:
                setPersistentVariable(name, value);
                break;
            case DYNAMIC:
                plugin.getLogger().warning("Cannot directly set dynamic variables");
                break;
        }
    }

    @Override
    public DataValue getVariable(String name, VariableScope scope, String context) {
        if (name == null || scope == null) {
            return null;
        }
        
        switch (scope) {
            case LOCAL:
                return getLocalVariable(context, name);
            case GLOBAL:
                return getGlobalVariable(name);
            case PLAYER:
                try {
                    UUID playerId = UUID.fromString(context);
                    return getPlayerVariable(playerId, name);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid player UUID: " + context);
                    return null;
                }
            case SERVER:
                return getServerVariable(name);
            case PERSISTENT:
                return getPersistentVariable(name);
            case DYNAMIC:
                DynamicVariable dynamicVar = dynamicVariables.get(name);
                return dynamicVar != null ? dynamicVar.getValue() : null;
            default:
                return null;
        }
    }
    
    /**
     * Enhanced variable resolution with fallback mechanism
     * Tries to resolve a variable by searching through scopes in order of precedence
     * @param name The name of the variable to resolve
     * @param context The context identifier (player UUID, script ID, etc.)
     * @return The resolved variable value or null if not found
     */
    public DataValue resolveVariable(String name, String context) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        
        // Try to resolve the variable with a smart scope resolution order
        DataValue value;
        
        // 1. Try player scope if context is a valid UUID
        if (context != null) {
            try {
                UUID playerId = UUID.fromString(context);
                value = getPlayerVariable(playerId, name);
                if (value != null) return value;
            } catch (IllegalArgumentException e) {
                // Not a valid UUID, continue with other scopes
            }
            
            // 2. Try local scope
            value = getLocalVariable(context, name);
            if (value != null) return value;
        }
        
        // 3. Try global scope
        value = getGlobalVariable(name);
        if (value != null) return value;
        
        // 4. Try server scope
        value = getServerVariable(name);
        if (value != null) return value;
        
        // 5. Try persistent scope
        value = getPersistentVariable(name);
        if (value != null) return value;
        
        // 6. Try dynamic variables
        DynamicVariable dynamicVar = dynamicVariables.get(name);
        if (dynamicVar != null) return dynamicVar.getValue();
        
        // Variable not found in any scope
        return null;
    }
    
    /**
     * Enhanced variable resolution with explicit scope precedence
     * Allows specifying the order in which scopes should be checked
     * @param name The name of the variable to resolve
     * @param context The context identifier
     * @param scopes The order of scopes to check
     * @return The resolved variable value or null if not found
     */
    public DataValue resolveVariableWithScopes(String name, String context, VariableScope... scopes) {
        if (name == null || name.isEmpty() || scopes == null || scopes.length == 0) {
            return null;
        }
        
        for (VariableScope scope : scopes) {
            DataValue value = getVariable(name, scope, context);
            if (value != null) {
                return value;
            }
        }
        
        return null;
    }
    
    @Override
    public void registerDynamicVariable(String name, IVariableManager.DynamicVariable variable) {
        if (name != null && variable != null) {
            dynamicVariables.put(name, variable);
            updateMetadata(name, VariableScope.DYNAMIC, ValueType.ANY);
        }
    }
    
    public void registerDynamicVariable(String name, Supplier<DataValue> supplier, ValueType type) {
        if (name == null || supplier == null || type == null) {
            return;
        }
        registerDynamicVariable(name, new DynamicVariableImpl(name, supplier, type));
    }
    
    @Override
    public void unregisterDynamicVariable(String name) {
        if (name != null) {
            dynamicVariables.remove(name);
            variableMetadata.remove("dynamic_" + name);
        }
    }
    
    @Override
    public VariableMetadata getVariableMetadata(String name) {
        if (name == null) {
            return null;
        }
        return variableMetadata.get(name);
    }
    
    @Override
    public Map<String, VariableMetadata> getAllVariableMetadata() {
        return new HashMap<>(variableMetadata);
    }
    
    @Override
    public void clearScope(VariableScope scope, String identifier) {
        switch (scope) {
            case LOCAL:
                localVariables.remove(identifier);
                break;
            case GLOBAL:
                globalVariables.remove(identifier);
                break;
            case PLAYER:
                try {
                    playerVariables.remove(UUID.fromString(identifier));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "Invalid player UUID: " + identifier, e);
                }
                break;
            case SERVER:
                serverVariables.clear();
                saveServerVariables();
                break;
            case PERSISTENT:
                persistentVariables.clear();
                savePersistentVariables();
                break;
            case DYNAMIC:
                // Don't clear dynamic variables as they're managed by the system
                break;
        }
    }
    
    @Override
    public void savePersistentData() {
        savePersistentVariables();
        saveServerVariables();
    }
    
    @Override
    public void loadPersistentData() {
        loadPersistentVariables();
        loadServerVariables();
    }
    
    @Override
    public boolean hasVariable(String name, VariableScope scope, String context) {
        if (name == null || scope == null) {
            return false;
        }
        
        switch (scope) {
            case LOCAL:
                return context != null && localVariables.containsKey(context) && 
                       localVariables.get(context).containsKey(name);
            case GLOBAL:
                return globalVariables.values().stream()
                    .anyMatch(map -> map.containsKey(name));
            case PLAYER:
                try {
                    UUID playerId = UUID.fromString(context);
                    return playerVariables.containsKey(playerId) && 
                           playerVariables.get(playerId).containsKey(name);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            case SERVER:
                return serverVariables.containsKey(name);
            case PERSISTENT:
                return persistentVariables.containsKey(name);
            case DYNAMIC:
                return dynamicVariables.containsKey(name);
            default:
                return false;
        }
    }
    
    @Override
    public void removeVariable(String name, VariableScope scope, String context) {
        if (name == null || scope == null) {
            return;
        }
        
        switch (scope) {
            case LOCAL:
                if (context != null) {
                    Map<String, DataValue> contextVars = localVariables.get(context);
                    if (contextVars != null) {
                        contextVars.remove(name);
                        if (contextVars.isEmpty()) {
                            localVariables.remove(context);
                        }
                        variableMetadata.remove("local_" + context + "_" + name);
                    }
                }
                break;
            case GLOBAL:
                globalVariables.computeIfPresent("global", (k, v) -> {
                    v.remove(name);
                    return v.isEmpty() ? null : v;
                });
                variableMetadata.remove("global_" + name);
                break;
            case PLAYER:
                try {
                    UUID playerId = UUID.fromString(context);
                    Map<String, DataValue> playerVars = playerVariables.get(playerId);
                    if (playerVars != null) {
                        playerVars.remove(name);
                        if (playerVars.isEmpty()) {
                            playerVariables.remove(playerId);
                        }
                        variableMetadata.remove("player_" + playerId + "_" + name);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid player UUID: " + context);
                }
                break;
            case SERVER:
                serverVariables.remove(name);
                variableMetadata.remove("server_" + name);
                saveServerVariables();
                break;
            case PERSISTENT:
                persistentVariables.remove(name);
                variableMetadata.remove("persistent_" + name);
                savePersistentVariables();
                break;
            case DYNAMIC:
                unregisterDynamicVariable(name);
                break;
        }
    }
    
    private void loadPersistentVariables() {
        if (persistentFile == null || !persistentFile.exists()) {
            return;
        }
        
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(persistentFile);
            for (String key : config.getKeys(false)) {
                try {
                    Object value = config.get(key);
                    if (value != null) {
                        persistentVariables.put(key, DataValue.of(value));
                        updateMetadata(key, VariableScope.PERSISTENT, ValueType.fromObject(value));
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load persistent variable '" + key + "': " + e.getMessage());
                }
            }
            plugin.getLogger().info("Loaded " + persistentVariables.size() + " persistent variables");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load persistent variables", e);
        }
    }
    
    private void savePersistentVariables() {
        if (persistentFile == null) {
            return;
        }
        
        try {
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, DataValue> entry : persistentVariables.entrySet()) {
                config.set(entry.getKey(), entry.getValue().getValue());
            }
            config.save(persistentFile);
            plugin.getLogger().fine("Saved " + persistentVariables.size() + " persistent variables");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save persistent variables", e);
        }
    }
    
    private void loadServerVariables() {
        if (serverVarsFile == null || !serverVarsFile.exists()) {
            return;
        }
        
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(serverVarsFile);
            for (String key : config.getKeys(false)) {
                try {
                    Object value = config.get(key);
                    if (value != null) {
                        serverVariables.put(key, DataValue.of(value));
                        updateMetadata(key, VariableScope.SERVER, ValueType.fromObject(value));
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load server variable '" + key + "': " + e.getMessage());
                }
            }
            plugin.getLogger().info("Loaded " + serverVariables.size() + " server variables");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load server variables", e);
        }
    }
    
    private void saveServerVariables() {
        if (serverVarsFile == null) {
            return;
        }
        
        try {
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, DataValue> entry : serverVariables.entrySet()) {
                config.set(entry.getKey(), entry.getValue().getValue());
            }
            config.save(serverVarsFile);
            plugin.getLogger().fine("Saved " + serverVariables.size() + " server variables");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save server variables", e);
        }
    }
    

    
    // Dynamic variable implementation
    private static class DynamicVariableImpl implements IVariableManager.DynamicVariable {
        private final String name;
        private final Supplier<DataValue> supplier;
        private final ValueType type;

        public DynamicVariableImpl(String name, Supplier<DataValue> supplier, ValueType type) {
            this.name = name;
            this.supplier = supplier;
            this.type = type;
        }

        @Override
        public DataValue get() {
            return supplier.get();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public DataValue getValue() {
            return get();
        }

        public ValueType getType() {
            return type;
        }
    }
    
    private void initializeStorage() {
        File dataFolder = new File(plugin.getDataFolder(), VARIABLES_FOLDER);
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().severe("Failed to create variables directory: " + dataFolder.getAbsolutePath());
            return;
        }
        
        // Initialize persistent variables file
        persistentFile = new File(dataFolder, "persistent_vars.yml");
        if (!persistentFile.exists()) {
            try {
                if (!persistentFile.createNewFile()) {
                    plugin.getLogger().severe("Failed to create persistent variables file");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create persistent variables file: " + e.getMessage());
            }
        }
        persistentConfig = YamlConfiguration.loadConfiguration(persistentFile);
        
        // Initialize server variables file
        serverVarsFile = new File(dataFolder, "server_vars.yml");
        if (!serverVarsFile.exists()) {
            try {
                if (!serverVarsFile.createNewFile()) {
                    plugin.getLogger().severe("Failed to create server variables file");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create server variables file: " + e.getMessage());
            }
        }
    }
    

    
    // === LOCAL VARIABLES (SCRIPT SCOPE) ===
    
    @Override
    public void setLocalVariable(String context, String name, DataValue value) {
        if (name == null || context == null) {
            return;
        }
        
        if (value == null) {
            // Remove the variable if it exists
            Map<String, DataValue> scriptVars = localVariables.get(context);
            if (scriptVars != null) {
                scriptVars.remove(name);
                if (scriptVars.isEmpty()) {
                    localVariables.remove(context);
                }
                variableMetadata.remove("local_" + context + "_" + name);
            }
        } else {
            // Set the variable
            localVariables.computeIfAbsent(context, k -> new HashMap<>()).put(name, value);
            updateMetadata(name, VariableScope.LOCAL, value.getType());
        }
    }
    
    @Override
    public DataValue getLocalVariable(String context, String name) {
        if (name == null || context == null) {
            return null;
        }
        Map<String, DataValue> vars = localVariables.get(context);
        return vars != null ? vars.get(name) : null;
    }
    
    // Remove the @Override annotation since this method doesn't exist in the interface
    public void clearLocalVariables(String context) {
        if (context != null) {
            localVariables.remove(context);
            // Remove all local variable metadata for this context
            String prefix = "local_" + context + "_";
            variableMetadata.entrySet().removeIf(entry -> 
                entry.getKey().startsWith(prefix));
            plugin.getLogger().fine("Cleared local variables for context: " + context);
        }
    }
    
    // === GLOBAL VARIABLES (WORLD SCOPE) ===
    
    @Override
    public void setGlobalVariable(String name, DataValue value) {
        if (name == null || value == null) {
            return;
        }
        
        // Use default world for global variables
        String worldId = "global";
        
        globalVariables.computeIfAbsent(worldId, k -> new HashMap<>()).put(name, value);
        updateMetadata(name, VariableScope.GLOBAL, value.getType());
        notifyPlayersInWorld(worldId, name, value);
        plugin.getLogger().fine("Set global variable: " + name + " = " + value.asString());
    }
    
    @Override
    public DataValue getGlobalVariable(String name) {
        if (name == null) {
            return null;
        }
        return globalVariables.values().stream()
                .map(map -> map.get(name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
    
    // Remove the @Override annotation since this method doesn't exist in the interface
    public Map<String, DataValue> getAllGlobalVariables() {
        // Return all global variables from all worlds combined
        Map<String, DataValue> result = new HashMap<>();
        for (Map<String, DataValue> worldVars : globalVariables.values()) {
            if (worldVars != null) {
                result.putAll(worldVars);
            }
        }
        return result;
    }
    
    // Remove the @Override annotation since this method doesn't exist in the interface
    public void clearGlobalVariables() {
        // Clear all global variables from all worlds
        globalVariables.clear();
        // Remove all global variable metadata
        variableMetadata.entrySet().removeIf(entry -> 
            entry.getValue().getScope() == VariableScope.GLOBAL);
        plugin.getLogger().fine("Cleared all global variables");
    }
    
    // === PLAYER VARIABLES ===
    
    @Override
    public void setPlayerVariable(UUID playerId, String name, DataValue value) {
        if (playerId == null || name == null || value == null) {
            return;
        }
        
        playerVariables.computeIfAbsent(playerId, k -> new HashMap<>()).put(name, value);
        updateMetadata(name, VariableScope.PLAYER, value.getType());
        plugin.getLogger().fine("Set player variable: " + playerId + "." + name + " = " + value.asString());
    }
    
    @Override
    public DataValue getPlayerVariable(UUID playerId, String name) {
        if (playerId == null || name == null) {
            return null;
        }
        Map<String, DataValue> vars = playerVariables.get(playerId);
        return vars != null ? vars.get(name) : null;
    }
    
    @Override
    public Map<String, DataValue> getPlayerVariables(UUID playerId) {
        if (playerId == null) {
            return Collections.emptyMap();
        }
        Map<String, DataValue> vars = playerVariables.get(playerId);
        return vars != null ? new HashMap<>(vars) : Collections.emptyMap();
    }
    
    @Override
    public void clearPlayerVariables(UUID playerId) {
        if (playerId != null) {
            playerVariables.remove(playerId);
            // Remove metadata for all player variables
            variableMetadata.entrySet().removeIf(entry -> 
                entry.getKey().startsWith("player_" + playerId + "_"));
        }
    }
    
    // === SERVER VARIABLES ===
    
    @Override
    public void setServerVariable(String name, DataValue value) {
        if (name == null || value == null) {
            return;
        }
        
        serverVariables.put(name, value);
        updateMetadata(name, VariableScope.SERVER, value.getType());
        plugin.getLogger().fine("Set server variable: " + name + " = " + value.asString());
        saveServerVariables();
    }
    
    @Override
    public DataValue getServerVariable(String name) {
        if (name == null) {
            return null;
        }
        return serverVariables.get(name);
    }
    
    @Override
    public Map<String, DataValue> getServerVariables() {
        return new HashMap<>(serverVariables);
    }
    
    @Override
    public void clearServerVariables() {
        serverVariables.clear();
        // Remove metadata for all server variables
        variableMetadata.entrySet().removeIf(entry -> 
            entry.getKey().startsWith("server_"));
    }
    
    // === PERSISTENT VARIABLES ===
    
    @Override
    public void setPersistentVariable(String name, DataValue value) {
        if (name == null || value == null) {
            return;
        }
        persistentVariables.put(name, value);
        updateMetadata(name, VariableScope.PERSISTENT, value.getType());
        savePersistentVariables();
    }
    
    @Override
    public DataValue getPersistentVariable(String name) {
        return name != null ? persistentVariables.get(name) : null;
    }
    
    @Override
    public Map<String, DataValue> getAllPersistentVariables() {
        return new HashMap<>(persistentVariables);
    }
    
    @Override
    public void clearPersistentVariables() {
        persistentVariables.clear();
        // Remove metadata for all persistent variables
        variableMetadata.entrySet().removeIf(entry -> 
            entry.getKey().startsWith("persistent_")
        );
        savePersistentVariables();
    }
    
    private void registerDynamicVariables() {
        // Register built-in dynamic variables
        registerDynamicVariable("time", () -> DataValue.of(System.currentTimeMillis()));
        registerDynamicVariable("random", () -> DataValue.of(Math.random()));
        registerDynamicVariable("online_players", () -> 
            DataValue.of(plugin.getServer().getOnlinePlayers().size()));
    }
    
    @Override
    public void incrementPlayerVariable(UUID playerId, String name, double amount) {
        if (playerId == null || name == null) {
            return;
        }
        
        DataValue current = getPlayerVariable(playerId, name);
        double currentValue = 0.0;
        
        if (current != null && current.getType() == ValueType.NUMBER) {
            Object value = current.getValue();
            if (value instanceof Number) {
                currentValue = ((Number) value).doubleValue();
            }
        }
        
        setPlayerVariable(playerId, name, DataValue.of(currentValue + amount));
    }
    
    /**
     * Gets all variables across all scopes for a given context
     * @param context The context identifier (player UUID, script ID, etc.)
     * @return A map of variable names to their values across all scopes
     */
    public Map<String, DataValue> getAllVariables(String context) {
        Map<String, DataValue> allVariables = new HashMap<>();
        
        // Add local variables if context is provided
        if (context != null) {
            Map<String, DataValue> locals = localVariables.get(context);
            if (locals != null) {
                allVariables.putAll(locals);
            }
            
            // Add player variables if context is a valid UUID
            try {
                UUID playerId = UUID.fromString(context);
                Map<String, DataValue> playerVars = playerVariables.get(playerId);
                if (playerVars != null) {
                    allVariables.putAll(playerVars);
                }
            } catch (IllegalArgumentException e) {
                // Context is not a player UUID, continue
            }
        }
        
        // Add global variables
        for (Map<String, DataValue> worldVars : globalVariables.values()) {
            if (worldVars != null) {
                allVariables.putAll(worldVars);
            }
        }
        
        // Add server variables
        allVariables.putAll(serverVariables);
        
        // Add persistent variables
        allVariables.putAll(persistentVariables);
        
        // Add dynamic variables
        for (Map.Entry<String, DynamicVariable> entry : dynamicVariables.entrySet()) {
            allVariables.put(entry.getKey(), entry.getValue().getValue());
        }
        
        return allVariables;
    }
    
    private void updateMetadata(String name, VariableScope scope, ValueType type) {
        if (name == null || scope == null || type == null) {
            return;
        }
        
        String key;
        switch (scope) {
            case SERVER:
                key = "server_" + name;
                break;
            case PERSISTENT:
                key = "persistent_" + name;
                break;
            case GLOBAL:
                key = "global_" + name;
                break;
            case LOCAL:
                key = "local_" + name;
                break;
            case PLAYER:
                key = "player_" + name;
                break;
            case DYNAMIC:
                key = "dynamic_" + name;
                break;
            default:
                key = scope.name().toLowerCase() + "_" + name;
        }
        
        long now = System.currentTimeMillis();
        
        variableMetadata.compute(key, (k, existing) -> 
            new VariableMetadata(name, scope, type, now));
    }
    
    private void notifyPlayersInWorld(String worldId, String variableName, DataValue value) {
        if (worldId == null || variableName == null || value == null) {
            return;
        }
        
        // Handle global variables - broadcast to all players
        if ("global".equals(worldId)) {
            String message = String.format("Global variable %s updated to: %s", 
                variableName, value.asString());
            plugin.getServer().getOnlinePlayers().forEach(player -> 
                player.sendMessage(message));
            return;
        }
        
        // Handle world-specific variables
        plugin.getServer().getWorlds().stream()
            .filter(world -> world.getUID().toString().equals(worldId))
            .findFirst()
            .ifPresent(world -> {
                String message = String.format("Variable %s updated to: %s", 
                    variableName, value.asString());
                world.getPlayers().forEach(player -> 
                    player.sendMessage(message));
            });
    }
}