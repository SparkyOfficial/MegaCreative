package com.megacreative.coding.variables;

import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Interface for managing variables in the MegaCreative plugin.
 * Handles different scopes of variables: local, global, persistent, and dynamic.
 * Enhanced with improved scope resolution capabilities.
 */
public interface IVariableManager {
    
    // Variable scope types
    enum VariableScope {
        LOCAL,      // Script/function scope
        GLOBAL,     // World scope
        PERSISTENT, // Server scope (saved between restarts)
        DYNAMIC,    // Computed on access
        PLAYER,     // Player-specific variables
        SERVER      // Server-wide variables
    }
    
    // Variable metadata class
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
        public void updateLastModified() { this.lastModified = System.currentTimeMillis(); }
    }
    
    // Dynamic variable interface
    @FunctionalInterface
    interface DynamicVariable {
        DataValue get();
        
        default String getName() {
            return toString();
        }
        
        default DataValue getValue() {
            return get();
        }
    }
    
    // Core variable operations
    void setVariable(String name, DataValue value, VariableScope scope, String context);
    DataValue getVariable(String name, VariableScope scope, String context);
    boolean hasVariable(String name, VariableScope scope, String context);
    void removeVariable(String name, VariableScope scope, String context);
    
    // Enhanced variable resolution with fallback mechanism
    default DataValue resolveVariable(String name, String context) {
        // Default implementation will be overridden in VariableManager
        return getVariable(name, VariableScope.LOCAL, context);
    }
    
    // Enhanced variable resolution with explicit scope precedence
    default DataValue resolveVariableWithScopes(String name, String context, VariableScope... scopes) {
        // Default implementation will be overridden in VariableManager
        if (scopes.length > 0) {
            return getVariable(name, scopes[0], context);
        }
        return null;
    }
    
    // Enhanced variable resolution using GameEvent context
    default DataValue resolveVariableWithContext(String name, GameEvent context) {
        // Default implementation will be overridden in VariableManager
        if (context != null && context.getPlayer() != null) {
            return resolveVariable(name, context.getPlayer().getUniqueId().toString());
        }
        return null;
    }
    
    // Convenience methods for different scopes
    void setLocalVariable(String context, String name, DataValue value);
    DataValue getLocalVariable(String context, String name);
    
    void setGlobalVariable(String name, DataValue value);
    DataValue getGlobalVariable(String name);
    
    // Player-specific variables
    void setPlayerVariable(UUID playerId, String name, DataValue value);
    DataValue getPlayerVariable(UUID playerId, String name);
    Map<String, DataValue> getPlayerVariables(UUID playerId);
    void clearPlayerVariables(UUID playerId);
    
    // Server variables
    void setServerVariable(String name, DataValue value);
    DataValue getServerVariable(String name);
    Map<String, DataValue> getServerVariables();
    void clearServerVariables();
    
    // Persistent variables
    void setPersistentVariable(String name, DataValue value);
    DataValue getPersistentVariable(String name);
    Map<String, DataValue> getAllPersistentVariables();
    void clearPersistentVariables();
    
    // Variable metadata
    VariableMetadata getVariableMetadata(String name);
    Map<String, VariableMetadata> getAllVariableMetadata();
    
    // Dynamic variables
    void registerDynamicVariable(String name, DynamicVariable variable);
    void unregisterDynamicVariable(String name);
    
    // Utility methods
    void clearScope(VariableScope scope, String context);
    void savePersistentData();
    void loadPersistentData();
    
    // Additional utility methods
    void incrementPlayerVariable(UUID playerId, String name, double amount);
    
    // Enhanced utility methods
    default Map<String, DataValue> getAllVariables(String context) {
        // Default implementation will be overridden in VariableManager
        return new HashMap<>();
    }
}