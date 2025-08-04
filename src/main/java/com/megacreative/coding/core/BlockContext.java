package com.megacreative.coding.core;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Контекст выполнения блока.
 * Содержит всю необходимую информацию для выполнения блока.
 */
public class BlockContext {
    private final JavaPlugin plugin;
    private final String scriptId;
    private final Player player;
    private final Event event;
    private final Map<String, Object> variables;
    private final Map<String, Object> localVars;
    
    private BlockContext(Builder builder) {
        this.plugin = builder.plugin;
        this.scriptId = builder.scriptId;
        this.player = builder.player;
        this.event = builder.event;
        this.variables = new HashMap<>(builder.variables);
        this.localVars = new HashMap<>();
    }
    
    // Геттеры
    public JavaPlugin getPlugin() { return plugin; }
    public String getScriptId() { return scriptId; }
    public Player getPlayer() { return player; }
    public Event getEvent() { return event; }
    
    /**
     * Получает значение глобальной переменной.
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Устанавливает значение глобальной переменной.
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }
    
    /**
     * Получает значение локальной переменной.
     */
    public Object getLocalVariable(String name) {
        return localVars.get(name);
    }
    
    /**
     * Устанавливает значение локальной переменной.
     */
    public void setLocalVariable(String name, Object value) {
        localVars.put(name, value);
    }
    
    /**
     * Создает новый Builder для контекста.
     */
    public static Builder builder(JavaPlugin plugin) {
        return new Builder(plugin);
    }
    
    /**
     * Builder для создания контекста.
     */
    public static class Builder {
        private final JavaPlugin plugin;
        private String scriptId = UUID.randomUUID().toString();
        private Player player;
        private Event event;
        private Map<String, Object> variables = new HashMap<>();
        
        public Builder(JavaPlugin plugin) {
            this.plugin = plugin;
        }
        
        public Builder scriptId(String scriptId) {
            this.scriptId = scriptId;
            return this;
        }
        
        public Builder player(Player player) {
            this.player = player;
            return this;
        }
        
        public Builder event(Event event) {
            this.event = event;
            return this;
        }
        
        public Builder variables(Map<String, Object> variables) {
            this.variables = new HashMap<>(variables);
            return this;
        }
        
        public Builder addVariable(String name, Object value) {
            this.variables.put(name, value);
            return this;
        }
        
        public BlockContext build() {
            return new BlockContext(this);
        }
    }
}
