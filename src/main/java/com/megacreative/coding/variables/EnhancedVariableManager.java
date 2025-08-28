package com.megacreative.coding.variables;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import lombok.extern.java.Log;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class EnhancedVariableManager {
    
    private final MegaCreative plugin;
    
    private final Map<String, DataValue> localVariables = new ConcurrentHashMap<>();
    private final Map<String, Map<String, DataValue>> worldVariables = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, DataValue>> playerVariables = new ConcurrentHashMap<>();
    private final Map<String, DataValue> serverVariables = new ConcurrentHashMap<>();
    
    public EnhancedVariableManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    public void setVariable(String variableName, DataValue value, CreativeWorld world, Player player) {
        VariableScope scope = VariableScope.extractScope(variableName);
        String baseName = VariableScope.extractBaseName(variableName);
        
        switch (scope) {
            case LOCAL -> setLocalVariable(baseName, value);
            case WORLD -> setWorldVariable(world != null ? world.getId() : "default", baseName, value);
            case PLAYER -> setPlayerVariable(player != null ? player.getUniqueId() : null, baseName, value);
            case SERVER -> setServerVariable(baseName, value);
        }
    }
    
    public DataValue getVariable(String variableName, CreativeWorld world, Player player) {
        VariableScope scope = VariableScope.extractScope(variableName);
        String baseName = VariableScope.extractBaseName(variableName);
        
        return switch (scope) {
            case LOCAL -> getLocalVariable(baseName);
            case WORLD -> getWorldVariable(world != null ? world.getId() : "default", baseName);
            case PLAYER -> getPlayerVariable(player != null ? player.getUniqueId() : null, baseName);
            case SERVER -> getServerVariable(baseName);
        };
    }
    
    public void setLocalVariable(String name, DataValue value) {
        localVariables.put(name, value);
    }
    
    public DataValue getLocalVariable(String name) {
        return localVariables.get(name);
    }
    
    public void setWorldVariable(String worldId, String name, DataValue value) {
        worldVariables.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>()).put(name, value);
    }
    
    public DataValue getWorldVariable(String worldId, String name) {
        Map<String, DataValue> worldVars = worldVariables.get(worldId);
        return worldVars != null ? worldVars.get(name) : null;
    }
    
    public void setPlayerVariable(UUID playerId, String name, DataValue value) {
        if (playerId == null) return;
        playerVariables.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(name, value);
    }
    
    public DataValue getPlayerVariable(UUID playerId, String name) {
        if (playerId == null) return null;
        Map<String, DataValue> playerVars = playerVariables.get(playerId);
        return playerVars != null ? playerVars.get(name) : null;
    }
    
    public void setServerVariable(String name, DataValue value) {
        serverVariables.put(name, value);
    }
    
    public DataValue getServerVariable(String name) {
        return serverVariables.get(name);
    }
}