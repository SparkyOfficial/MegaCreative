package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.VariableScope;
import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    
    // Debugging control
    private boolean paused = false;
    private boolean stepping = false;
    private boolean cancelled = false;

    // Ссылка на менеджер переменных
    private final VariableManager variableManager;
    
    // Идентификаторы для доступа к переменным
    private final String scriptId; // Идентификатор скрипта
    private final String worldId;  // Идентификатор мира
    
    // No more redundant caches - using VariableManager directly

    /**
     * Creates a new execution context with all required parameters.
     * @param plugin The main plugin instance (required)
     * @param player The player associated with this context (can be null)
     * @param creativeWorld The world where the script is executing (can be null)
     * @param event The event that triggered the execution (can be null)
     * @param blockLocation The location of the block being executed (can be null)
     * @param currentBlock The current code block being executed (can be null)
     * @throws IllegalArgumentException if plugin is null
     */
    public ExecutionContext(MegaCreative plugin, Player player, CreativeWorld creativeWorld, Event event, 
                          Location blockLocation, CodeBlock currentBlock) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
        this.player = player;
        this.creativeWorld = creativeWorld;
        this.event = event;
        this.blockLocation = blockLocation != null ? blockLocation.clone() : null;
        this.currentBlock = currentBlock;
        this.variableManager = plugin.getVariableManager();
        this.scriptId = currentBlock != null ? currentBlock.getId().toString() : "global";
        this.worldId = creativeWorld != null ? creativeWorld.getId() : "global";
    }
    
    public ExecutionContext withCurrentBlock(CodeBlock currentBlock, Location newLocation) {
        return new ExecutionContext(this.plugin, this.player, this.creativeWorld, this.event, newLocation, currentBlock);
    }

    /**
     * Устанавливает локальную переменную скрипта
     */
    public void setVariable(String name, Object value) {
        if (scriptId == null) {
            throw new IllegalStateException("Cannot set variable: script ID is not available");
        }
        variableManager.setLocalVariable(scriptId, name, DataValue.fromObject(value));
    }
    
    /**
     * Получает локальную переменную скрипта
     */
    /**
     * Gets a variable value by name.
     * @param name The name of the variable
     * @return The variable value, or null if not found
     */
    public Object getVariable(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        
        // Get from variable manager
        DataValue value = variableManager.getLocalVariable(scriptId, name);
        return value != null ? value.getValue() : null;
    }
    
    /**
     * Получает все переменные текущего скрипта
     */
    public Map<String, Object> getVariables() {
        if (scriptId == null) {
            return new HashMap<>();
        }
        Map<String, DataValue> scriptVars = variableManager.getPlayerVariables(UUID.fromString(scriptId));
        Map<String, Object> result = new HashMap<>();
        scriptVars.forEach((k, v) -> result.put(k, v != null ? v.getValue() : null));
        return result;
    }
    
    /**
     * Устанавливает глобальную переменную мира
     */
    public void setGlobalVariable(String name, Object value) {
        if (worldId == null) {
            throw new IllegalStateException("Cannot set global variable: world ID is not available");
        }
        variableManager.setGlobalVariable(name, DataValue.fromObject(value));
    }
    
    /**
     * Sets a server-wide persistent variable
     * @param name The name of the variable
     * @param value The value to set (must be a DataValue)
     */
    public void setServerVariable(String name, DataValue value) {
        if (variableManager == null) {
            throw new IllegalStateException("VariableManager is not available");
        }
        variableManager.setServerVariable(name, value);
    }
    
    /**
     * Gets a server variable value by name
     * @param name The name of the server variable
     * @return The variable value or null if not found
     */
    public Object getServerVariable(String name) {
        if (variableManager == null) {
            return null;
        }
        DataValue value = variableManager.getServerVariable(name);
        return value != null ? value.getValue() : null;
    }
    
    /**
     * Получает глобальную переменную мира
     */
    public Object getGlobalVariable(String name) {
        if (worldId == null) {
            return null;
        }
        DataValue value = variableManager.getGlobalVariable(name);
        return value != null ? value.getValue() : null;
    }
    
    /**
     * Устанавливает переменную игрока
     */
    public void setPlayerVariable(String name, Object value) {
        if (player == null) {
            throw new IllegalStateException("Cannot set player variable: player is not available");
        }
        variableManager.setPlayerVariable(player.getUniqueId(), name, DataValue.fromObject(value));
    }
    
    /**
     * Получает переменную игрока
     */
    public Object getPlayerVariable(String name) {
        if (player == null) {
            return null;
        }
        DataValue value = variableManager.getPlayerVariable(player.getUniqueId(), name);
        return value != null ? value.getValue() : null;
    }
    
    
    // === МЕТОДЫ ДЛЯ РАБОТЫ СО СПИСКАМИ ===
    
    /**
     * Создает или обновляет список
     */
    public void setList(String name, List<Object> list) {
        setVariable(name, new ArrayList<>(list));
    }
    
    /**
     * Получает список
     */
    @SuppressWarnings("unchecked")
    public List<Object> getList(String name) {
        Object value = getVariable(name);
        return value instanceof List ? (List<Object>) value : null;
    }
    
    /**
     * Добавляет элемент в список
     */
    @SuppressWarnings("unchecked")
    public void addToList(String name, Object element) {
        List<Object> list = getList(name);
        if (list == null) {
            list = new ArrayList<>();
            setVariable(name, list);
        }
        list.add(element);
    }
    
    /**
     * Удаляет элемент из списка
     */
    @SuppressWarnings("unchecked")
    public void removeFromList(String name, Object element) {
        List<Object> list = getList(name);
        if (list != null) {
            list.remove(element);
        }
    }
    
    // --- МЕТОДЫ ДЛЯ РАБОТЫ С БУЛЕВЫМИ ПЕРЕМЕННЫМИ ---
    
    /**
     * Sets a boolean variable in the execution context.
     * @param name The name of the variable
     * @param value The boolean value to set
     */
    public void setBoolean(String name, boolean value) {
        if (scriptId != null) {
            variableManager.setLocalVariable(scriptId, name, DataValue.of(value));
        }
    }
    
    /**
     * Sets a number variable in the execution context.
     * @param name The name of the variable
     * @param value The number value to set (can be null to remove the variable)
     */
    public void setNumber(String name, Number value) {
        if (value != null) {
            setVariable(name, value);
        } else if (scriptId != null) {
            variableManager.removeVariable(name, VariableScope.LOCAL, scriptId);
        }
    }
    
    /**
     * Получает числовую переменную
     */
    public Number getNumber(String name) {
        Object value = getVariable(name);
        if (value instanceof Number) {
            return (Number) value;
        }
        return 0; // Default value
    }
    
    /**
     * Gets a boolean variable by name
     * @param name The name of the boolean variable
     * @return The boolean value or false if not found or not a boolean
     */
    public Boolean getBoolean(String name) {
        Object value = getVariable(name);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return false; // Default value
    }
    
    /**
     * Получает число как int
     */
    public int getInt(String name) {
        Number number = getNumber(name);
        return number != null ? number.intValue() : 0;
    }
    
    /**
     * Получает число как double
     */
    /**
     * Gets a number as a double
     */
    public double getDouble(String name) {
        Number number = getNumber(name);
        return number != null ? number.doubleValue() : 0.0;
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
    
    // Debugging control methods
    
    public boolean isPaused() {
        return paused;
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    public boolean isStepping() {
        return stepping;
    }
    
    public void setStepping(boolean stepping) {
        this.stepping = stepping;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
    
    /**
     * Checks if debug mode is enabled for this execution context
     * Debug mode provides additional logging and feedback during script execution
     */
    public boolean isDebugMode() {
        // Check if player has debug permissions
        if (player != null && player.hasPermission("megacreative.debug")) {
            return true;
        }
        
        // Default to false for safety
        return false;
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
