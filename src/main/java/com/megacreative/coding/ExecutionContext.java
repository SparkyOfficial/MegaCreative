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

    private final MegaCreative plugin; // Ссылка на основной плагин
    private final Player player; // Игрок, который вызвал событие (может быть null)
    private final CreativeWorld creativeWorld; // Мир, в котором выполняется скрипт
    private final Event event; // Само событие, которое вызвало скрипт
    private final Location blockLocation; // Локация выполняемого блока (может быть null)
    private final CodeBlock currentBlock; // Текущий выполняемый блок (может быть null)

    // Переменные скрипта (String -> Object)
    private Map<String, Object> variables = new HashMap<>();
    
    // --- РАСШИРЕННЫЕ ТИПЫ ДАННЫХ ---
    // Списки (массивы) для хранения коллекций данных
    private Map<String, List<Object>> lists = new HashMap<>();
    
    // Булевы переменные для логических операций
    private Map<String, Boolean> booleans = new HashMap<>();
    
    // Числовые переменные с поддержкой int и double
    private Map<String, Number> numbers = new HashMap<>();

    /**
     * Создает новый контекст с указанным текущим блоком.
     * @param currentBlock Текущий блок для выполнения
     * @param newLocation Новая локация блока
     * @return Новый контекст с обновленным блоком и локацией
     */
    public ExecutionContext withCurrentBlock(CodeBlock currentBlock, Location newLocation) {
        ExecutionContext newContext = new ExecutionContext(this.plugin, this.player, this.creativeWorld, this.event, newLocation, currentBlock);
        newContext.variables = new HashMap<>(this.variables);
        newContext.lists = new HashMap<>(this.lists);
        newContext.booleans = new HashMap<>(this.booleans);
        newContext.numbers = new HashMap<>(this.numbers);
        return newContext;
    }

    /**
     * Устанавливает переменную
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }
    
    /**
     * Получает переменную
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Получает все переменные
     */
    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
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
    
    // Конструктор
    public ExecutionContext(MegaCreative plugin, Player player, CreativeWorld creativeWorld, Event event, Location blockLocation, CodeBlock currentBlock) {
        this.plugin = plugin;
        this.player = player;
        this.creativeWorld = creativeWorld;
        this.event = event;
        this.blockLocation = blockLocation;
        this.currentBlock = currentBlock;
    }
    
    // Геттеры
    public MegaCreative getPlugin() {
        return plugin;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public CreativeWorld getCreativeWorld() {
        return creativeWorld;
    }
    
    public Event getEvent() {
        return event;
    }
    
    public Location getBlockLocation() {
        return blockLocation;
    }
    
    public CodeBlock getCurrentBlock() {
        return currentBlock;
    }
    
    /**
     * Gets script ID for variable resolution
     */
    public String getScriptId() {
        // Generate script ID based on current block or world
        if (currentBlock != null) {
            return "script_" + currentBlock.getId().toString();
        }
        if (creativeWorld != null) {
            return "script_" + creativeWorld.getId();
        }
        return "script_unknown";
    }
    
    /**
     * Gets world ID for variable resolution  
     */
    public String getWorldId() {
        return creativeWorld != null ? creativeWorld.getId() : "unknown_world";
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private MegaCreative plugin;
        private Player player;
        private CreativeWorld creativeWorld;
        private Event event;
        private Location blockLocation;
        private CodeBlock currentBlock;
        
        public Builder plugin(MegaCreative plugin) {
            this.plugin = plugin;
            return this;
        }
        
        public Builder player(Player player) {
            this.player = player;
            return this;
        }
        
        public Builder creativeWorld(CreativeWorld creativeWorld) {
            this.creativeWorld = creativeWorld;
            return this;
        }
        
        public Builder event(Event event) {
            this.event = event;
            return this;
        }
        
        public Builder blockLocation(Location blockLocation) {
            this.blockLocation = blockLocation;
            return this;
        }
        
        public Builder currentBlock(CodeBlock currentBlock) {
            this.currentBlock = currentBlock;
            return this;
        }
        
        public ExecutionContext build() {
            return new ExecutionContext(plugin, player, creativeWorld, event, blockLocation, currentBlock);
        }
    }
}
