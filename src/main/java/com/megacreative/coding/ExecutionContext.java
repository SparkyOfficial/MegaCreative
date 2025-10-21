package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.events.CustomEvent;

import com.megacreative.coding.executors.AdvancedExecutionEngine.ExecutionMode;
import com.megacreative.coding.executors.AdvancedExecutionEngine.Priority;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.Constants;
import com.megacreative.coding.ChestParser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Хранит всю информацию, необходимую для выполнения одного скрипта.
 * Передается между блоками во время исполнения.
 * Enhanced with improved variable scope resolution.
 */
public class ExecutionContext {

    
    private final String executionId;
    private CodeScript script;
    private Player playerField;
    private String trigger;
    private long startTime;
    private CodeBlock currentBlockField;
    private final boolean cancelledField = false;

    private final MegaCreative plugin; 
    private final Player player; 
    private final CreativeWorld creativeWorld; 
    private final CustomEvent event; 
    private final Location blockLocation; 
    private final CodeBlock currentBlock; 
    
    
    private boolean paused = false;
    private boolean stepping = false;
    private boolean cancelled = false;
    
    
    private boolean breakFlag = false;
    private boolean continueFlag = false;
    
    
    private int instructionCount = 0;
    
	
    private ExecutionMode executionMode = ExecutionMode.SYNCHRONOUS;
    private Priority priority = Priority.NORMAL;
    private int maxInstructions = 1000;
    private long executionTimeout = 0;
    private final long executionStartTime = System.currentTimeMillis();

	
    private final VariableManager variableManager;
    
	
    private final String scriptId; 
    private final String worldId;  
    
	
    private boolean lastConditionResult = false;
    
	
    
	
    private ChestParser chestParser;
    
    /**
     * Full constructor with all parameters
     */
    public ExecutionContext(MegaCreative plugin, Player player, CreativeWorld creativeWorld, CustomEvent event, 
                          Location blockLocation, CodeBlock currentBlock) {
        this.plugin = plugin;
        this.player = player;
        this.creativeWorld = creativeWorld;
        this.event = event;
        this.blockLocation = blockLocation;
        this.currentBlock = currentBlock;
        this.executionId = UUID.randomUUID().toString();
        this.startTime = System.currentTimeMillis();
        this.scriptId = creativeWorld != null ? creativeWorld.getId() : Constants.GLOBAL_SCOPE_ID;
        this.worldId = this.scriptId;
        this.variableManager = plugin != null ? plugin.getServiceRegistry().getVariableManager() : null;
        
		
        if (blockLocation != null) {
            this.chestParser = ChestParser.forAdjacentChest(blockLocation);
        }
    }
    
    /**
     * Constructor for executor engine compatibility
     */
    public ExecutionContext(MegaCreative plugin, String executionId, CodeScript script, Player player, String trigger) {
        this.executionId = executionId;
        this.script = script;
        this.playerField = player;
        this.trigger = trigger;
        this.startTime = System.currentTimeMillis();
        
        this.plugin = plugin;
        this.player = player;
        this.creativeWorld = null;
        this.event = null;
        this.blockLocation = null;
        this.currentBlock = null;
        this.currentBlockField = null;
        this.variableManager = this.plugin != null ? this.plugin.getServiceRegistry().getVariableManager() : null;
        this.scriptId = Constants.GLOBAL_SCOPE_ID;
        this.worldId = Constants.GLOBAL_SCOPE_ID;
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public CodeScript getScript() {
        return script;
    }
    
    public Player getPlayer() {
        return playerField != null ? playerField : player;
    }
    
    public String getTrigger() {
        return trigger;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public CodeBlock getCurrentBlock() {
        return currentBlockField != null ? currentBlockField : currentBlock;
    }
    
    public void setCurrentBlock(CodeBlock currentBlock) {
        this.currentBlockField = currentBlock;
    }
    
    public MegaCreative getPlugin() {
        return plugin;
    }
    
    /**
     * Creates a successful execution result
     */
    public ExecutionResult createResult(boolean success, String message) {
        return new ExecutionResult.Builder()
            .success(success)
            .message(message)
            .executedBlock(getCurrentBlock())
            .executor(getPlayer())
            .executionTime(System.currentTimeMillis() - startTime)
            .build();
    }
    
    /**
     * Creates an error execution result
     */
    public ExecutionResult createErrorResult(String message) {
        return createErrorResult(message, null);
    }
    
    /**
     * Creates an error execution result with exception
     */
    public ExecutionResult createErrorResult(String message, Throwable error) {
        return new ExecutionResult.Builder()
            .success(false)
            .message(message)
            .executedBlock(getCurrentBlock())
            .executor(getPlayer())
            .error(error)
            .executionTime(System.currentTimeMillis() - startTime)
            .build();
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

        // Condition plugin.getLogger() != null is always true when reached
        // Removed redundant null check
        if (plugin != null) {
            plugin.getLogger().info("Debug: Variable '" + name + "' set to '" + value + "' for player " + (player != null ? player.getName() : "null"));
        }
    }
    
    /**
     * Gets a variable value by name with enhanced scope resolution.
     * @param name The name of the variable
     * @return The variable value, or null if not found
     */
    public Object getVariable(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        
		
        String context = getPlayerContext();
        DataValue value = variableManager.resolveVariable(name, context);
        return value != null ? value.getValue() : null;
    }
    
    /**
     * Gets a variable with explicit scope.
     * @param name The name of the variable
     * @param scope The scope to look in
     * @return The variable value, or null if not found
     */
    public Object getVariable(String name, VariableScope scope) {
        if (name == null || name.isEmpty() || scope == null) {
            return null;
        }
        
        String context = getPlayerContext();
        DataValue value = variableManager.getVariable(name, scope, context);
        return value != null ? value.getValue() : null;
    }
    
    /**
     * Gets all variables across all scopes for this execution context.
     * @return A map of variable names to their values
     */
    public Map<String, Object> getVariables() {
        String context = getPlayerContext();
        Map<String, DataValue> dataValues = variableManager.getAllVariables(context);
        Map<String, Object> result = new HashMap<>();
        
        dataValues.forEach((name, value) -> {
            result.put(name, value != null ? value.getValue() : null);
        });
        
        return result;
    }
    
    /**
     * Gets the player context for variable resolution.
     * @return The player UUID as string, or script ID if no player
     */
    private String getPlayerContext() {
        if (player != null) {
            return player.getUniqueId().toString();
        }
        return scriptId != null ? scriptId : Constants.GLOBAL_SCOPE_ID;
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
            variableManager.setVariable(name, null, VariableScope.LOCAL, scriptId);
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
        return 0; 
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
        return false; 
    }
    
    /**
     * Получает число как int
     */
    public int getInt(String name) {
        Number number = getNumber(name);
        return number != null ? number.intValue() : 0;
    }
    
    /**
     * Gets a number as a double
     */
    public double getDouble(String name) {
        Number number = getNumber(name);
        return number != null ? number.doubleValue() : 0.0;
    }
    
    
	
    
    public CreativeWorld getCreativeWorld() {
        return creativeWorld;
    }
    
    public CustomEvent getEvent() {
        return event;
    }
    
    public Location getBlockLocation() {
        return blockLocation;
    }
    
	
    
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
    
	
    public boolean hasBreakFlag() {
        return breakFlag;
    }
    
    public void setBreakFlag(boolean breakFlag) {
        this.breakFlag = breakFlag;
		
        // Condition plugin.getLogger() != null is always true when reached
        // Removed redundant null check
        if (plugin != null) {
            plugin.getLogger().fine(Constants.BREAK_FLAG_SET + breakFlag);
        }
    }
    
    public void clearBreakFlag() {
        this.breakFlag = false;
		
        // Condition plugin.getLogger() != null is always true when reached
        // Removed redundant null check
        if (plugin != null) {
            plugin.getLogger().fine(Constants.BREAK_FLAG_CLEARED);
        }
    }
    
    public boolean hasContinueFlag() {
        return continueFlag;
    }
    
    public void setContinueFlag(boolean continueFlag) {
        this.continueFlag = continueFlag;
		
        // Condition plugin.getLogger() != null is always true when reached
        // Removed redundant null check
        if (plugin != null) {
            plugin.getLogger().fine(Constants.CONTINUE_FLAG_SET + continueFlag);
        }
    }
    
    public void clearContinueFlag() {
        this.continueFlag = false;
		
        // Condition plugin.getLogger() != null is always true when reached
        // Removed redundant null check
        if (plugin != null) {
            plugin.getLogger().fine(Constants.CONTINUE_FLAG_CLEARED);
        }
    }
    
    /**
     * Gets script ID for variable resolution
     */
    public String getScriptId() {
		
        if (currentBlock != null) {
            return Constants.SCRIPT_ID_PREFIX + currentBlock.getId().toString();
        }
        if (creativeWorld != null) {
            return Constants.SCRIPT_ID_PREFIX + creativeWorld.getId();
        }
        return Constants.SCRIPT_UNKNOWN;
    }
    
    /**
     * Gets world ID for variable resolution  
     */
    public String getWorldId() {
        return creativeWorld != null ? creativeWorld.getId() : Constants.UNKNOWN_WORLD;
    }
    
    /**
     * Checks if debug mode is enabled for this execution context
     * Debug mode provides additional logging and feedback during script execution
     */
    public boolean isDebugMode() {
		
        if (player != null && player.hasPermission(Constants.DEBUG_PERMISSION)) {
            return true;
        }
        
		
        return false;
    }
    
    /**
     * Sets the result of the last condition evaluation
     * @param result The result of the condition evaluation
     */
    public void setLastConditionResult(boolean result) {
        this.lastConditionResult = result;
    }
    
    /**
     * Gets the result of the last condition evaluation
     * @return The result of the last condition evaluation
     */
    public boolean getLastConditionResult() {
        return this.lastConditionResult;
    }
    
    /**
     * Gets the instruction count for this execution context
     * @return The number of instructions executed
     */
    public int getInstructionCount() {
        return instructionCount;
    }
    
    /**
     * Increments the instruction counter
     */
    public void incrementInstructionCount() {
        instructionCount++;
    }
    
    /**
     * Resets the instruction counter
     */
    public void resetInstructionCount() {
        instructionCount = 0;
    }
	
    
    /**
     * Gets the execution mode for this context
     */
    public ExecutionMode getExecutionMode() {
        return executionMode;
    }
    
    /**
     * Sets the execution mode
     */
    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }
    
    /**
     * Gets the execution priority
     */
    public Priority getPriority() {
        return priority;
    }
    
    /**
     * Sets the execution priority
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    /**
     * Gets the maximum allowed instructions
     */
    public int getMaxInstructions() {
        return maxInstructions;
    }
    
    /**
     * Sets the maximum allowed instructions
     */
    public void setMaxInstructions(int maxInstructions) {
        this.maxInstructions = maxInstructions;
    }
    
    /**
     * Gets the execution timeout timestamp
     */
    public long getExecutionTimeout() {
        return executionTimeout;
    }
    
    /**
     * Sets the execution timeout
     */
    public void setExecutionTimeout(long executionTimeout) {
        this.executionTimeout = executionTimeout;
    }
    
    /**
     * Checks if execution has timed out
     */
    public boolean isTimedOut() {
        return executionTimeout > 0 && System.currentTimeMillis() > executionTimeout;
    }
    
    /**
     * Gets the execution start time
     */
    public long getExecutionStartTime() {
        return executionStartTime;
    }
    
    /**
     * Gets the current execution duration in milliseconds
     */
    public long getExecutionDuration() {
        return System.currentTimeMillis() - executionStartTime;
    }
    
    /**
     * Checks if the instruction limit has been exceeded
     */
    public boolean isInstructionLimitExceeded() {
        return instructionCount > maxInstructions;
    }
    
    /**
     * Gets a variable value by name, returning the raw DataValue.
     * @param name The name of the variable
     * @return The variable value as a DataValue, or null if not found
     */
    public DataValue getVariableAsDataValue(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
		
        String context = getPlayerContext();
        return variableManager.resolveVariable(name, context);
    }
    
    /**
     * Gets a variable with explicit scope, returning the raw DataValue.
     * @param name The name of the variable
     * @param scope The scope to look in
     * @return The variable value as a DataValue, or null if not found
     */
    public DataValue getVariableAsDataValue(String name, VariableScope scope) {
        if (name == null || name.isEmpty() || scope == null) {
            return null;
        }
        
        String context = getPlayerContext();
        return variableManager.getVariable(name, scope, context);
    }
    
    /**
     * Gets text parameter from adjacent chest
     * @param slot The chest slot (0-26)
     * @return The text value or null if not found
     */
    public String getChestText(int slot) {
        if (chestParser != null) {
            return chestParser.getText(slot);
        }
        return null;
    }
    
    /**
     * Gets numeric parameter from adjacent chest
     * @param slot The chest slot (0-26)
     * @return The numeric value or 0 if not found
     */
    public double getChestNumber(int slot) {
        if (chestParser != null) {
            return chestParser.getNumber(slot);
        }
        return 0;
    }
    
    /**
     * Gets location parameter from adjacent chest
     * @param slot The chest slot (0-26)
     * @return The location or null if not found
     */
    public Location getChestLocation(int slot) {
        if (chestParser != null) {
            return chestParser.getLocation(slot);
        }
        return null;
    }
    
    /**
     * Gets item parameter from adjacent chest
     * @param slot The chest slot (0-26)
     * @return The item stack or null if not found
     */
    public ItemStack getChestItem(int slot) {
        if (chestParser != null) {
            return chestParser.getItem(slot);
        }
        return null;
    }
    
    /**
     * Gets all items from adjacent chest
     * @return List of all items or empty list if no chest found
     */
    public List<ItemStack> getChestItems() {
        if (chestParser != null) {
            return chestParser.getAllItems();
        }
        return new ArrayList<>();
    }
	
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private MegaCreative plugin;
        private Player player;
        private CreativeWorld creativeWorld;
        private CustomEvent event;
        private Location blockLocation;
        private CodeBlock currentBlock;
		
        private ExecutionMode executionMode = ExecutionMode.SYNCHRONOUS;
        private Priority priority = Priority.NORMAL;
        private int maxInstructions = 1000;
        
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
        
        public Builder event(CustomEvent event) {
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
		
        public Builder executionMode(ExecutionMode executionMode) {
            this.executionMode = executionMode;
            return this;
        }
        
        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder maxInstructions(int maxInstructions) {
            this.maxInstructions = maxInstructions;
            return this;
        }
        
        public ExecutionContext build() {
            ExecutionContext context = new ExecutionContext(plugin, player, creativeWorld, event, blockLocation, currentBlock);
            context.executionMode = this.executionMode;
            context.priority = this.priority;
            context.maxInstructions = this.maxInstructions;
            return context;
        }
    }
}