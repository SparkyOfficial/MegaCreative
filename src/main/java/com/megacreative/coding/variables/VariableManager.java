package com.megacreative.coding.variables;

import com.megacreative.MegaCreative;
import com.megacreative.coding.Constants;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages variables for the MegaCreative plugin.
 * Handles different scopes of variables including local, global, player, and persistent variables.
 * Enhanced with improved scope resolution and fallback mechanisms.
 */
public class VariableManager implements IVariableManager {
    
    private final MegaCreative plugin;
    private final Map<String, DataValue> localVariables = new ConcurrentHashMap<>();
    private final Map<String, Map<String, DataValue>> globalVariables = new ConcurrentHashMap<>();
    private final Map<String, DataValue> playerVariables = new ConcurrentHashMap<>();
    private final Map<String, DataValue> serverVariables = new ConcurrentHashMap<>();
    private final Map<String, DataValue> persistentVariables = new ConcurrentHashMap<>();
    private final Map<String, VariableMetadata> variableMetadata = new ConcurrentHashMap<>();
    private final File dataFolder;
    
    public VariableManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), Constants.VARIABLES_FOLDER);
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                plugin.getLogger().severe(Constants.FAILED_TO_CREATE_VARIABLES_DIR + dataFolder.getAbsolutePath());
            }
        }
    }

    @Override
    public void setVariable(String name, DataValue value, VariableScope scope, String context) {
        if (name == null || value == null || scope == null) {
            throw new IllegalArgumentException(Constants.NAME_VALUE_SCOPE_CANNOT_BE_NULL);
        }
        
        // Prevent setting dynamic variables directly
        if (name.startsWith(Constants.DYNAMIC_PREFIX)) {
            plugin.getLogger().warning(Constants.VARIABLE_MANAGER_NOT_AVAILABLE);
            return;
        }
        
        try {
            switch (scope) {
                case GLOBAL:
                    setGlobalVariable(name, value);
                    break;
                case LOCAL:
                    setLocalVariable(context, name, value);
                    break;
                case PLAYER:
                    // Context should be a player UUID
                    UUID playerId = UUID.fromString(context);
                    setPlayerVariable(playerId, name, value);
                    break;
                case SERVER:
                    setServerVariable(name, value);
                    break;
                case PERSISTENT:
                    setPersistentVariable(name, value);
                    break;
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, Constants.INVALID_PLAYER_UUID + context, e);
        }
    }

    public void setLocalVariable(String context, String name, DataValue value) {
        if (context == null || name == null) {
            throw new IllegalArgumentException(Constants.CONTEXT_AND_NAME_CANNOT_BE_NULL);
        }
        
        // Use context as script ID for local variables
        String key = Constants.LOCAL_PREFIX + context + "_" + name;
        localVariables.put(key, value);
        
        // Update metadata
        VariableMetadata metadata = new VariableMetadata(key, VariableScope.LOCAL, value.getType(), System.currentTimeMillis());
        variableMetadata.put(key, metadata);
    }

    public DataValue getLocalVariable(String context, String name) {
        if (context == null || name == null) {
            return null;
        }
        
        String key = Constants.LOCAL_PREFIX + context + "_" + name;
        return localVariables.get(key);
    }

    public void setGlobalVariable(String name, DataValue value) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        
        // Store in the global map under the "global" key
        globalVariables.compute(Constants.GLOBAL_KEY, (k, globalMap) -> {
            if (globalMap == null) {
                globalMap = new HashMap<>();
            }
            globalMap.put(name, value);
            return globalMap;
        });
        
        // Update metadata
        VariableMetadata metadata = new VariableMetadata(name, VariableScope.GLOBAL, value.getType(), System.currentTimeMillis());
        variableMetadata.put("global_" + name, metadata);
    }

    public DataValue getGlobalVariable(String name) {
        if (name == null) {
            return null;
        }
        
        Map<String, DataValue> globalMap = globalVariables.get(Constants.GLOBAL_KEY);
        return globalMap != null ? globalMap.get(name) : null;
    }

    public void setPlayerVariable(UUID playerId, String name, DataValue value) {
        if (playerId == null || name == null) {
            throw new IllegalArgumentException(Constants.PLAYER_ID_CANNOT_BE_NULL);
        }
        
        String key = Constants.PLAYER_PREFIX + playerId.toString() + "_" + name;
        playerVariables.put(key, value);
        
        // Update metadata
        VariableMetadata metadata = new VariableMetadata(key, VariableScope.PLAYER, value.getType(), System.currentTimeMillis());
        variableMetadata.put(key, metadata);
    }

    public DataValue getPlayerVariable(UUID playerId, String name) {
        if (playerId == null || name == null) {
            return null;
        }
        
        String key = Constants.PLAYER_PREFIX + playerId.toString() + "_" + name;
        return playerVariables.get(key);
    }

    public void setServerVariable(String name, DataValue value) {
        if (name == null) {
            throw new IllegalArgumentException(Constants.NAME_VALUE_SCOPE_CANNOT_BE_NULL);
        }
        
        serverVariables.put(name, value);
        
        // Update metadata
        VariableMetadata metadata = new VariableMetadata(name, VariableScope.SERVER, value.getType(), System.currentTimeMillis());
        variableMetadata.put("server_" + name, metadata);
    }

    public DataValue getServerVariable(String name) {
        if (name == null) {
            return null;
        }
        
        return serverVariables.get(name);
    }

    public void setPersistentVariable(String name, DataValue value) {
        if (name == null) {
            throw new IllegalArgumentException(Constants.NAME_VALUE_SCOPE_CANNOT_BE_NULL);
        }
        
        persistentVariables.put(name, value);
        
        // Update metadata
        VariableMetadata metadata = new VariableMetadata(name, VariableScope.PERSISTENT, value.getType(), System.currentTimeMillis());
        variableMetadata.put("persistent_" + name, metadata);
    }

    public DataValue getPersistentVariable(String name) {
        if (name == null) {
            return null;
        }
        
        return persistentVariables.get(name);
    }

    @Override
    public DataValue getVariable(String name, VariableScope scope, String context) {
        if (name == null || scope == null) {
            return null;
        }
        
        try {
            switch (scope) {
                case GLOBAL:
                    return getGlobalVariable(name);
                case LOCAL:
                    return getLocalVariable(context, name);
                case PLAYER:
                    // Context should be a player UUID
                    UUID playerId = UUID.fromString(context);
                    return getPlayerVariable(playerId, name);
                case SERVER:
                    return getServerVariable(name);
                case PERSISTENT:
                    return getPersistentVariable(name);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, Constants.INVALID_PLAYER_UUID + context, e);
            return null;
        }
        
        return null;
    }

    @Override
    public void clearScope(VariableScope scope, String identifier) {
        if (scope == null || identifier == null) {
            return;
        }
        
        switch (scope) {
            case GLOBAL:
                globalVariables.clear();
                break;
            case LOCAL:
                localVariables.entrySet().removeIf(entry -> entry.getKey().startsWith(Constants.LOCAL_PREFIX + identifier + "_"));
                break;
            case PLAYER:
                playerVariables.entrySet().removeIf(entry -> entry.getKey().startsWith(Constants.PLAYER_PREFIX + identifier + "_"));
                break;
            case SERVER:
                serverVariables.clear();
                break;
            case PERSISTENT:
                persistentVariables.clear();
                break;
        }
    }

    @Override
    public void savePersistentData() {
        // No-op for this implementation
    }

    @Override
    public void loadPersistentData() {
        // No-op for this implementation
    }

    @Override
    public boolean hasVariable(String name, VariableScope scope, String context) {
        if (name == null || scope == null) {
            return false;
        }
        
        try {
            switch (scope) {
                case GLOBAL:
                    return getGlobalVariable(name) != null;
                case LOCAL:
                    return getLocalVariable(context, name) != null;
                case PLAYER:
                    // Context should be a player UUID
                    UUID playerId = UUID.fromString(context);
                    return getPlayerVariable(playerId, name) != null;
                case SERVER:
                    return getServerVariable(name) != null;
                case PERSISTENT:
                    return getPersistentVariable(name) != null;
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, Constants.INVALID_PLAYER_UUID + context, e);
            return false;
        }
        
        return false;
    }

    @Override
    public void removeVariable(String name, VariableScope scope, String context) {
        if (name == null || scope == null) {
            return;
        }
        
        try {
            switch (scope) {
                case GLOBAL:
                    globalVariables.remove(name);
                    break;
                case LOCAL:
                    localVariables.remove(Constants.LOCAL_PREFIX + context + "_" + name);
                    break;
                case PLAYER:
                    // Context should be a player UUID
                    UUID playerId = UUID.fromString(context);
                    playerVariables.remove(Constants.PLAYER_PREFIX + playerId.toString() + "_" + name);
                    break;
                case SERVER:
                    serverVariables.remove(name);
                    break;
                case PERSISTENT:
                    persistentVariables.remove(name);
                    break;
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, Constants.INVALID_PLAYER_UUID + context, e);
        }
    }

    public String getPlayerContext(Player player) {
        return player != null ? player.getUniqueId().toString() : Constants.GLOBAL_SCOPE;
    }

    public String getScriptContext(String scriptId) {
        return scriptId != null ? scriptId : Constants.GLOBAL_SCOPE;
    }
    
    @Override
    public void incrementPlayerVariable(UUID playerId, String name, double amount) {
        DataValue currentValue = getPlayerVariable(playerId, name);
        double newValue = (currentValue != null ? currentValue.asNumber().doubleValue() : 0) + amount;
        setPlayerVariable(playerId, name, DataValue.of(newValue));
    }
    
    @Override
    public VariableMetadata getVariableMetadata(String name) {
        return variableMetadata.get(name);
    }
    
    @Override
    public Map<String, VariableMetadata> getAllVariableMetadata() {
        return new HashMap<>(variableMetadata);
    }
    
    @Override
    public void registerDynamicVariable(String name, DynamicVariable variable) {
        // Implementation would go here
    }
    
    @Override
    public void unregisterDynamicVariable(String name) {
        // Implementation would go here
    }
    
    @Override
    public Map<String, DataValue> getPlayerVariables(UUID playerId) {
        Map<String, DataValue> playerVars = new HashMap<>();
        for (Map.Entry<String, DataValue> entry : playerVariables.entrySet()) {
            if (entry.getKey().startsWith(Constants.PLAYER_PREFIX + playerId.toString() + "_")) {
                String varName = entry.getKey().substring((Constants.PLAYER_PREFIX + playerId.toString() + "_").length());
                playerVars.put(varName, entry.getValue());
            }
        }
        return playerVars;
    }
    
    @Override
    public void clearPlayerVariables(UUID playerId) {
        playerVariables.entrySet().removeIf(entry -> entry.getKey().startsWith(Constants.PLAYER_PREFIX + playerId.toString() + "_"));
    }
    
    @Override
    public Map<String, DataValue> getServerVariables() {
        return new HashMap<>(serverVariables);
    }
    
    @Override
    public void clearServerVariables() {
        serverVariables.clear();
    }
    
    /**
     * Gets all global variables
     */
    public Map<String, DataValue> getAllGlobalVariables() {
        Map<String, DataValue> globalMap = globalVariables.get("global");
        return globalMap != null ? new HashMap<>(globalMap) : new HashMap<>();
    }
    
    /**
     * Clears all global variables
     */
    public void clearGlobalVariables() {
        globalVariables.clear();
    }
    
    @Override
    public Map<String, DataValue> getAllPersistentVariables() {
        return new HashMap<>(persistentVariables);
    }
    
    @Override
    public void clearPersistentVariables() {
        persistentVariables.clear();
    }
    
    @Override
    public Map<String, DataValue> getAllVariables(String context) {
        Map<String, DataValue> allVars = new HashMap<>();
        
        // Add local variables
        for (Map.Entry<String, DataValue> entry : localVariables.entrySet()) {
            if (entry.getKey().startsWith("local_" + context + "_")) {
                String varName = entry.getKey().substring(("local_" + context + "_").length());
                allVars.put(varName, entry.getValue());
            }
        }
        
        // Add global variables
        Map<String, DataValue> globalMap = globalVariables.get("global");
        if (globalMap != null) {
            allVars.putAll(globalMap);
        }
        
        // Add server variables
        allVars.putAll(serverVariables);
        
        // Add persistent variables
        allVars.putAll(persistentVariables);
        
        return allVars;
    }
    
    @Override
    public DataValue resolveVariable(String name, String context) {
        // First check local scope
        DataValue value = getLocalVariable(context, name);
        if (value != null) return value;
        
        // Then check global scope
        value = getGlobalVariable(name);
        if (value != null) return value;
        
        // Then check server scope
        value = getServerVariable(name);
        if (value != null) return value;
        
        // Finally check persistent scope
        return getPersistentVariable(name);
    }
    
    @Override
    public DataValue resolveVariableWithScopes(String name, String context, VariableScope... scopes) {
        for (VariableScope scope : scopes) {
            DataValue value = getVariable(name, scope, context);
            if (value != null) return value;
        }
        return null;
    }
}