package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Хранит всю информацию, необходимую для выполнения одного скрипта.
 * Передается между блоками во время исполнения.
 */
public class ExecutionContext {

    private final MegaCreative plugin;
    private final Player player;
    private final CreativeWorld creativeWorld;
    private final Event event;
    private final Location blockLocation;
    private final CodeBlock currentBlock;
    private final Map<String, Object> variables;
    private final Map<String, List<Object>> lists;
    private final Map<String, Boolean> booleans;
    private final Map<String, Number> numbers;

    private ExecutionContext(ExecutionContextBuilder builder) {
        this.plugin = builder.plugin;
        this.player = builder.player;
        this.creativeWorld = builder.creativeWorld;
        this.event = builder.event;
        this.blockLocation = builder.blockLocation;
        this.currentBlock = builder.currentBlock;
        this.variables = builder.variables != null ? builder.variables : new HashMap<>();
        this.lists = builder.lists != null ? builder.lists : new HashMap<>();
        this.booleans = builder.booleans != null ? builder.booleans : new HashMap<>();
        this.numbers = builder.numbers != null ? builder.numbers : new HashMap<>();
    }

    public static ExecutionContextBuilder builder() {
        return new ExecutionContextBuilder();
    }

    public MegaCreative getPlugin() { return plugin; }
    public Player getPlayer() { return player; }
    public CreativeWorld getCreativeWorld() { return creativeWorld; }
    public Event getEvent() { return event; }
    public Location getBlockLocation() { return blockLocation; }
    public CodeBlock getCurrentBlock() { return currentBlock; }

    public ExecutionContext withCurrentBlock(CodeBlock currentBlock, Location newLocation) {
        return ExecutionContext.builder()
                .plugin(this.plugin)
                .player(this.player)
                .creativeWorld(this.creativeWorld)
                .event(this.event)
                .blockLocation(newLocation)
                .currentBlock(currentBlock)
                .variables(new HashMap<>(this.variables))
                .lists(new HashMap<>(this.lists))
                .booleans(new HashMap<>(this.booleans))
                .numbers(new HashMap<>(this.numbers))
                .build();
    }

    /**
     * Устанавливает переменную
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
        
        // Логируем изменение переменной для отладки
        if (player != null && plugin != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
            plugin.getScriptDebugger().onVariableAccess(player, name, value, "set");
        }
    }
    
    /**
     * Получает переменную
     */
    public Object getVariable(String name) {
        Object value = variables.get(name);
        
        // Логируем чтение переменной для отладки
        if (player != null && plugin != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
            plugin.getScriptDebugger().onVariableAccess(player, name, value, "get");
        }
        
        return value;
    }
    
    /**
     * Получает все переменные
     */
    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }
    
    /**
     * Удаляет переменную
     */
    public void removeVariable(String name) {
        Object value = variables.remove(name);
        
        // Логируем удаление переменной для отладки
        if (player != null && plugin != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
            plugin.getScriptDebugger().onVariableAccess(player, name, value, "delete");
        }
    }
    
    // --- МЕТОДЫ ДЛЯ РАБОТЫ СО СПИСКАМИ ---
    
    /**
     * Создает или обновляет список
     */
    public void setList(String name, List<Object> list) {
        lists.put(name, new ArrayList<>(list));
    }
    
    /**
     * Получает список
     */
    public List<Object> getList(String name) {
        return lists.get(name);
    }
    
    /**
     * Добавляет элемент в список
     */
    public void addToList(String name, Object element) {
        lists.computeIfAbsent(name, k -> new ArrayList<>()).add(element);
    }
    
    /**
     * Удаляет элемент из списка
     */
    public void removeFromList(String name, Object element) {
        List<Object> list = lists.get(name);
        if (list != null) {
            list.remove(element);
        }
    }
    
    // --- МЕТОДЫ ДЛЯ РАБОТЫ С БУЛЕВЫМИ ПЕРЕМЕННЫМИ ---
    
    /**
     * Устанавливает булеву переменную
     */
    public void setBoolean(String name, boolean value) {
        booleans.put(name, value);
    }
    
    /**
     * Получает булеву переменную
     */
    public Boolean getBoolean(String name) {
        return booleans.get(name);
    }
    
    // --- МЕТОДЫ ДЛЯ РАБОТЫ С ЧИСЛАМИ ---
    
    /**
     * Устанавливает числовую переменную
     */
    public void setNumber(String name, Number value) {
        numbers.put(name, value);
    }
    
    /**
     * Получает числовую переменную
     */
    public Number getNumber(String name) {
        return numbers.get(name);
    }
    
    /**
     * Получает число как int
     */
    public int getInt(String name) {
        Number number = numbers.get(name);
        return number != null ? number.intValue() : 0;
    }
    
    /**
     * Получает число как double
     */
    public double getDouble(String name) {
        Number number = numbers.get(name);
        return number != null ? number.doubleValue() : 0.0;
    }

    public static class ExecutionContextBuilder {
        private MegaCreative plugin;
        private Player player;
        private CreativeWorld creativeWorld;
        private Event event;
        private Location blockLocation;
        private CodeBlock currentBlock;
        private Map<String, Object> variables;
        private Map<String, List<Object>> lists;
        private Map<String, Boolean> booleans;
        private Map<String, Number> numbers;

        public ExecutionContextBuilder plugin(MegaCreative plugin) {
            this.plugin = plugin;
            return this;
        }

        public ExecutionContextBuilder player(Player player) {
            this.player = player;
            return this;
        }

        public ExecutionContextBuilder creativeWorld(CreativeWorld creativeWorld) {
            this.creativeWorld = creativeWorld;
            return this;
        }

        public ExecutionContextBuilder event(Event event) {
            this.event = event;
            return this;
        }

        public ExecutionContextBuilder blockLocation(Location blockLocation) {
            this.blockLocation = blockLocation;
            return this;
        }

        public ExecutionContextBuilder currentBlock(CodeBlock currentBlock) {
            this.currentBlock = currentBlock;
            return this;
        }

        public ExecutionContextBuilder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public ExecutionContextBuilder lists(Map<String, List<Object>> lists) {
            this.lists = lists;
            return this;
        }

        public ExecutionContextBuilder booleans(Map<String, Boolean> booleans) {
            this.booleans = booleans;
            return this;
        }

        public ExecutionContextBuilder numbers(Map<String, Number> numbers) {
            this.numbers = numbers;
            return this;
        }

        public ExecutionContext build() {
            return new ExecutionContext(this);
        }
    }
}
