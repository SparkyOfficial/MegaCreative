package com.megacreative.coding.core;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер переменных для системы визуального программирования.
 * Управляет глобальными и локальными переменными, а также их областями видимости.
 */
public class VariableManager {
    private final Map<String, Variable> globalVariables = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Variable>> playerVariables = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Variable>> scriptVariables = new ConcurrentHashMap<>();
    
    /**
     * Устанавливает значение глобальной переменной.
     */
    public void setGlobal(String name, Object value) {
        globalVariables.put(name, new Variable(name, value, Variable.Scope.GLOBAL));
    }
    
    /**
     * Получает значение глобальной переменной.
     */
    public Object getGlobal(String name) {
        Variable var = globalVariables.get(name);
        return var != null ? var.getValue() : null;
    }
    
    /**
     * Устанавливает значение переменной игрока.
     */
    public void setPlayerVariable(UUID playerId, String name, Object value) {
        playerVariables
            .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
            .put(name, new Variable(name, value, Variable.Scope.PLAYER));
    }
    
    /**
     * Получает значение переменной игрока.
     */
    public Object getPlayerVariable(UUID playerId, String name) {
        Map<String, Variable> vars = playerVariables.get(playerId);
        return (vars != null && vars.containsKey(name)) ? vars.get(name).getValue() : null;
    }
    
    /**
     * Устанавливает значение переменной скрипта.
     */
    public void setScriptVariable(String scriptId, String name, Object value) {
        scriptVariables
            .computeIfAbsent(UUID.fromString(scriptId), k -> new ConcurrentHashMap<>())
            .put(name, new Variable(name, value, Variable.Scope.SCRIPT));
    }
    
    /**
     * Получает значение переменной скрипта.
     */
    public Object getScriptVariable(String scriptId, String name) {
        Map<String, Variable> vars = scriptVariables.get(UUID.fromString(scriptId));
        return (vars != null && vars.containsKey(name)) ? vars.get(name).getValue() : null;
    }
    
    /**
     * Получает переменную по имени, проверяя все области видимости в порядке приоритета.
     */
    public Object getVariable(Player player, String scriptId, String name) {
        // Сначала проверяем локальные переменные скрипта
        if (scriptId != null) {
            Object scriptVar = getScriptVariable(scriptId, name);
            if (scriptVar != null) return scriptVar;
        }
        
        // Затем переменные игрока
        if (player != null) {
            Object playerVar = getPlayerVariable(player.getUniqueId(), name);
            if (playerVar != null) return playerVar;
        }
        
        // И наконец глобальные переменные
        return getGlobal(name);
    }
    
    /**
     * Очищает все переменные игрока.
     */
    public void clearPlayerVariables(UUID playerId) {
        playerVariables.remove(playerId);
    }
    
    /**
     * Очищает все переменные скрипта.
     */
    public void clearScriptVariables(String scriptId) {
        scriptVariables.remove(UUID.fromString(scriptId));
    }
    
    /**
     * Внутренний класс для хранения информации о переменной.
     */
    public static class Variable {
        public enum Scope {
            GLOBAL,  // Глобальная переменная
            PLAYER,  // Переменная игрока
            SCRIPT   // Локальная переменная скрипта
        }
        
        private final String name;
        private Object value;
        private final Scope scope;
        private final long timestamp;
        
        public Variable(String name, Object value, Scope scope) {
            this.name = name;
            this.value = value;
            this.scope = scope;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Геттеры и сеттеры
        public String getName() { return name; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
        public Scope getScope() { return scope; }
        public long getTimestamp() { return timestamp; }
    }
}
