package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.VariableManagerImpl;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Default implementation of the ScriptEngine interface.
 * Combines the best features of ScriptExecutor and ExecutorEngine
 * with a focus on performance and extensibility.
 */
public class DefaultScriptEngine implements ScriptEngine {
    
    private final MegaCreative plugin;
    private final VariableManager variableManager;
    private final VisualDebugger debugger;
    private BlockConfigService blockConfigService;
    
    private final Map<BlockType, BlockAction> actionRegistry = new HashMap<>();
    private final Map<BlockType, BlockCondition> conditionRegistry = new HashMap<>();
    private final Map<String, BlockType> blockTypeCache = new ConcurrentHashMap<>();
    
    private static final int MAX_RECURSION_DEPTH = 100;  
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    
    private boolean initialized = false;
    
    public DefaultScriptEngine(MegaCreative plugin, VariableManager variableManager, VisualDebugger debugger, BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        this.debugger = debugger;
        this.blockConfigService = blockConfigService;
    }
    
    /**
     * Sets the BlockConfigService instance for this ScriptEngine.
     * This allows for runtime injection of the service after construction.
     * 
     * @param blockConfigService The BlockConfigService instance to use
     */
    public void setBlockConfigService(BlockConfigService blockConfigService) {
        if (this.blockConfigService != null) {
            plugin.getLogger().warning("BlockConfigService is being replaced in ScriptEngine");
        }
        this.blockConfigService = blockConfigService;
        
        // Clear the block type cache when the config service changes
        if (blockTypeCache != null) {
            blockTypeCache.clear();
        }
        
        // Re-register default actions and conditions with the new config service
        if (initialized) {
            registerDefaultActions();
            registerDefaultConditions();
        }
    }
    
    /**
     * Gets the number of registered actions.
     * @return The number of registered actions
     */
    public int getActionCount() {
        return actionRegistry.size();
    }
    
    /**
     * Gets the number of registered conditions.
     * @return The number of registered conditions
     */
    public int getConditionCount() {
        return conditionRegistry.size();
    }
    
    /**
     * Initializes the ScriptEngine with required dependencies
     * @param plugin The plugin instance
     * @param variableManager The variable manager to use (can be null to use default)
     * @param debugger The visual debugger to use (can be null to use default)
     */
    public void initialize(MegaCreative plugin, VariableManager variableManager, VisualDebugger debugger, BlockConfigService blockConfigService) {
        if (initialized) {
            return;
        }
        
        // Use provided debugger or create a default one
        if (debugger != null) {
            this.debugger = debugger;
        } else {
            this.debugger = new VisualDebugger(plugin);
        }
        
        // Register default actions and conditions
        registerDefaultActions();
        registerDefaultConditions();
        
        // Load block configurations and validate
        validateBlockConfigs();
        
        initialized = true;
        plugin.getLogger().info("DefaultScriptEngine initialized with " + 
            actionRegistry.size() + " actions and " + 
            conditionRegistry.size() + " conditions");
    }
    
    /**
     * Validates that all configured blocks have corresponding BlockType enums.
     */
    private void validateBlockConfigs() {
        int missingTypes = 0;
        
        for (BlockConfig config : blockConfigService.getAllBlockConfigs()) {
            BlockType blockType = BlockType.getByMaterialAndAction(
                config.getMaterial(), 
                config.getActionName()
            );
            
            if (blockType == null) {
                plugin.getLogger().warning("No BlockType found for: " + 
                    config.getMaterial() + "/" + config.getActionName() + 
                    " (" + config.getId() + ")");
                missingTypes++;
            } else if (!blockType.getMaterial().equals(config.getMaterial())) {
                plugin.getLogger().warning("Material mismatch for " + config.getId() + 
                    ": expected " + blockType.getMaterial() + ", got " + config.getMaterial());
            }
        }
        
        if (missingTypes > 0) {
            plugin.getLogger().warning("Found " + missingTypes + " blocks without matching BlockType enums");
        }
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, String trigger) {
        if (script == null || script.getMainBlock() == null) {
            return CompletableFuture.completedFuture(ExecutionResult.failure("Invalid script or empty main block"));
        }
        
        String executionId = UUID.randomUUID().toString();
        ExecutionContext context = new ExecutionContext(
            plugin, 
            player, 
            player != null ? plugin.getWorldManager().getWorld(player.getWorld().getName()) : null,
            null, // event
            null, // blockLocation
            script.getMainBlock()
        );
        
        activeExecutions.put(executionId, context);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Process the starting block
                CodeBlock startBlock = script.getStartBlock();
                if (startBlock != null) {
                    processBlock(startBlock, context);
                    return ExecutionResult.success("Script executed successfully");
                } else {
                    return ExecutionResult.error("No start block found in script");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error executing script: " + e.getMessage());
                e.printStackTrace();
                return ExecutionResult.error("Error executing script: " + e.getMessage());
            } finally {
                activeExecutions.remove(executionId);
            }
        });
    }
    
    @Override
    public void registerAction(BlockType type, BlockAction action) {
        if (type != null && action != null) {
            actionRegistry.put(type, action);
        }
    }
    
    @Override
    public void registerCondition(BlockType type, BlockCondition condition) {
        if (type != null && condition != null) {
            conditionRegistry.put(type, condition);
        }
    }
    
    @Override
    public VariableManager getVariableManager() {
        return variableManager;
    }
    
    @Override
    public VisualDebugger getDebugger() {
        return debugger;
    }
    
    @Override
    public boolean pauseExecution(String executionId) {
        ExecutionContext context = activeExecutions.get(executionId);
        if (context != null) {
            context.setPaused(true);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean resumeExecution(String executionId) {
        ExecutionContext context = activeExecutions.get(executionId);
        if (context != null && context.isPaused()) {
            context.setPaused(false);
            synchronized (context) {
                context.notifyAll();
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean stepExecution(String executionId) {
        ExecutionContext context = activeExecutions.get(executionId);
        if (context != null && context.isPaused()) {
            context.setStepping(true);
            return resumeExecution(executionId);
        }
        return false;
    }
    
    @Override
    public boolean stopExecution(String executionId) {
        ExecutionContext context = activeExecutions.remove(executionId);
        if (context != null) {
            context.setCancelled(true);
            return true;
        }
        return false;
    }
    
    /**
     * Processes a single code block in the execution context.
     * Handles debugging, error handling, and block execution.
     */
    private void processBlock(CodeBlock block, ExecutionContext context) {
        if (block == null || context.isCancelled()) {
            return;
        }

        // Handle pausing for debugging
        if (context.isPaused() && !context.isStepping()) {
            synchronized (context) {
                try {
                    context.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
        try {
            // Check recursion depth
            if (recursionDepth > MAX_RECURSION_DEPTH) {
                return ExecutionResult.failure("Maximum recursion depth (" + MAX_RECURSION_DEPTH + ") exceeded");
            }
            
            // Get block type using the new mapping system
            BlockType blockType = getBlockType(block.getMaterial(), block.getAction());
            if (blockType == null) {
                return ExecutionResult.failure("Unknown block type: " + block.getMaterial() + "/" + block.getAction());
            }
            
            // Update context with the current block
            context.setCurrentBlock(block);
            
            // Handle conditions and actions differently
            BlockAction action = actionRegistry.get(blockType);
            BlockCondition condition = conditionRegistry.get(blockType);
            
            if (action != null) {
                // Execute action block
                if (debugger != null) {
                    debugger.onBlockExecute(block, context);
                }
                return action.execute(block, context);
            } else if (condition != null) {
                // Handle condition block
                return handleCondition(block, context, recursionDepth);
            } else {
                return ExecutionResult.failure("No handler registered for block type: " + blockType);
            }
        } catch (Exception e) {
            String errorMsg = "Error processing block: " + e.getMessage();
            plugin.getLogger().severe(errorMsg);
            
            // Visual feedback for error
            if (context.getPlayer() != null && debugger != null) {
                debugger.highlightError(context.getPlayer(), block.getLocation(), errorMsg);
            }
            
            return ExecutionResult.failure(errorMsg);
        }
    }
    
    private ExecutionResult handleCondition(CodeBlock block, ExecutionContext context, int recursionDepth) {
        try {
            // Get the block type
            BlockType blockType = getBlockType(block.getMaterial(), block.getAction());
            if (blockType == null) {
                return ExecutionResult.failure("Unknown block type: " + block.getMaterial() + "/" + block.getAction());
            }
            
            // Get the condition handler
            BlockCondition condition = conditionRegistry.get(blockType);
            if (condition == null) {
                return ExecutionResult.failure("No condition handler registered for block type: " + blockType);
            }
            
            // Visual feedback for debugging
            if (debugger != null) {
                debugger.onConditionEvaluate(block, context);
            }
            
            // Evaluate the condition
            boolean result = condition.evaluate(block, context);
            
            // Handle the result
            if (result) {
                // Condition is true, execute the next block
                CodeBlock nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    return processBlock(nextBlock, context, recursionDepth + 1);
                }
                return ExecutionResult.success("Condition evaluated to true");
            } else {
                // Condition is false, skip the next block
                return ExecutionResult.success("Condition evaluated to false");
            }
        } catch (Exception e) {
            String errorMsg = "Error evaluating condition: " + e.getMessage();
            plugin.getLogger().severe(errorMsg);
            
            // Visual feedback for error
            if (context.getPlayer() != null && debugger != null) {
                debugger.highlightError(context.getPlayer(), block.getLocation(), errorMsg);
            }
            
            return ExecutionResult.failure(errorMsg);
        }
    }
    
    @Override
    public BlockType getBlockType(Material material, String actionName) {
        if (material == null || actionName == null) {
            return null;
        }
        
        String cacheKey = material.name() + ":" + actionName;
        return blockTypeCache.computeIfAbsent(cacheKey, k -> {
            // First try to get from BlockType enum
            BlockType blockType = BlockType.getByMaterialAndAction(material, actionName);
            
            // If not found, try to find a matching block config
            if (blockType == null && blockConfigService != null) {
                blockType = blockConfigService.getBlockType(material, actionName);
                
                // If still not found, try case-insensitive search
                if (blockType == null) {
                    for (BlockType type : BlockType.values()) {
                        if (type.getMaterial() == material && 
                            type.getActionName().equalsIgnoreCase(actionName)) {
                            return type;
                        }
                    }
                }
            }
            
            return blockType;
        });
    }
    
    // Helper methods for execution
    
    private void registerDefaultActions() {
        try {
            // Clear existing actions
            actionRegistry.clear();
            
            // Register static actions first
            registerAction(BlockType.ACTION_SEND_MESSAGE, new SendMessageAction());
            registerAction(BlockType.ACTION_TELEPORT_PLAYER, new TeleportAction());
            registerAction(BlockType.ACTION_GIVE_ITEM, new GiveItemAction());
            registerAction(BlockType.ACTION_SET_HEALTH, new SetHealthAction());
            registerAction(BlockType.ACTION_SET_GAMEMODE, new SetGamemodeAction());
            registerAction(BlockType.ACTION_PLAY_SOUND, new PlaySoundAction());
            
            // Register variable actions
            registerAction(BlockType.VARIABLE_SET, new SetVariableAction(variableManager));
            registerAction(BlockType.VARIABLE_GET, new GetVariableAction(variableManager));
            registerAction(BlockType.VARIABLE_ADD, new AddToVariableAction(variableManager));
            registerAction(BlockType.VARIABLE_SUBTRACT, new SubtractFromVariableAction(variableManager));
            
            // Register game actions
            registerAction(BlockType.GAME_ACTION_SPAWN_MOB, new SpawnMobAction());
            registerAction(BlockType.GAME_ACTION_EXPLOSION, new CreateExplosionAction());
            registerAction(BlockType.GAME_ACTION_WEATHER, new SetWeatherAction());
            registerAction(BlockType.GAME_ACTION_TIME, new SetTimeAction());
            
            // Register event handlers
            registerAction(BlockType.EVENT_PLAYER_JOIN, new PlayerJoinAction());
            registerAction(BlockType.EVENT_PLAYER_QUIT, new PlayerQuitAction());
            registerAction(BlockType.EVENT_PLAYER_INTERACT, new PlayerInteractAction());
            registerAction(BlockType.EVENT_PLAYER_MOVE, new PlayerMoveAction());
            registerAction(BlockType.EVENT_PLAYER_CHAT, new PlayerChatAction());
            registerAction(BlockType.EVENT_PLAYER_DEATH, new PlayerDeathAction());
            registerAction(BlockType.EVENT_PLAYER_RESPAWN, new PlayerRespawnAction());
            
            // Register actions from BlockConfigService if available
            if (blockConfigService != null) {
                int customActions = 0;
                for (BlockConfig config : blockConfigService.getAllBlockConfigs()) {
                    if (config.isAction() && config.isEnabled()) {
                        BlockType blockType = getBlockType(config.getMaterial(), config.getActionName());
                        if (blockType != null && !actionRegistry.containsKey(blockType)) {
                            BlockAction action = createActionFromConfig(config);
                            if (action != null) {
                                registerAction(blockType, action);
                                customActions++;
                            }
                        }
                    }
                }
                if (customActions > 0) {
                    plugin.getLogger().info("Registered " + customActions + " custom actions from config");
                }
            }
            
            plugin.getLogger().info("Registered " + actionRegistry.size() + " total actions");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register default actions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a BlockAction instance from a BlockConfig.
     * This method can be extended to support custom action types.
     * 
     * @param config The block configuration
     * @return A new BlockAction instance, or null if not supported
     */
    protected BlockAction createActionFromConfig(BlockConfig config) {
        // This is a basic implementation that creates a generic action
        // You can extend this method to support custom action types
        return new GenericBlockAction(config);
    }
    
    private void registerDefaultConditions() {
        try {
            // Clear existing conditions
            conditionRegistry.clear();
            
            // Register core conditions
            registerCondition(BlockType.CONDITION_IS_OP, new IsOpCondition());
            registerCondition(BlockType.CONDITION_HAS_ITEM, new HasItemCondition());
            registerCondition(BlockType.CONDITION_HAS_PERMISSION, new HasPermissionCondition());
            registerCondition(BlockType.CONDITION_IS_IN_WORLD, new IsInWorldCondition());
            registerCondition(BlockType.CONDITION_COMPARE_VARIABLE, new CompareVariableCondition());
            
            // Register conditions from BlockConfigService if available
            if (blockConfigService != null) {
                int customConditions = 0;
                for (BlockConfig config : blockConfigService.getAllBlockConfigs()) {
                    if (config.isCondition() && config.isEnabled()) {
                        BlockType blockType = getBlockType(config.getMaterial(), config.getActionName());
                        if (blockType != null && !conditionRegistry.containsKey(blockType)) {
                            BlockCondition condition = createConditionFromConfig(config);
                            if (condition != null) {
                                registerCondition(blockType, condition);
                                customConditions++;
                            }
                        }
                    }
                }
                if (customConditions > 0) {
                    plugin.getLogger().info("Registered " + customConditions + " custom conditions from config");
                }
            }
            
            // Variable conditions
            registerCondition(BlockType.CONDITION_VARIABLE_EQUALS, new VariableEqualsCondition(variableManager));
            
            // World conditions
            registerCondition(BlockType.CONDITION_IS_IN_REGION, new InRegionCondition());
            
            // Control flow conditions
            registerCondition(BlockType.IF_CONDITION, new IfCondition(variableManager));
            registerCondition(BlockType.ELSE_CONDITION, new ElseCondition());
            
            plugin.getLogger().info("Registered " + conditionRegistry.size() + " default conditions");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register default conditions: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Register more conditions as needed
    }
    
    /**
     * Creates a BlockCondition instance from a BlockConfig.
     * This method can be extended to support custom condition types.
     * 
     * @param config The block configuration
     * @return A new BlockCondition instance, or null if not supported
     */
    protected BlockCondition createConditionFromConfig(BlockConfig config) {
        // This is a basic implementation that creates a generic condition
        // You can extend this method to support custom condition types
        return new GenericBlockCondition(config);
    }
    
    // Getters for internal use
    
    protected Map<BlockType, BlockAction> getActionRegistry() {
        return actionRegistry;
    }
    
    protected Map<BlockType, BlockCondition> getConditionRegistry() {
        return conditionRegistry;
    }
    
    protected Map<String, ExecutionContext> getActiveExecutions() {
        return activeExecutions;
    }
    
    /**
     * Gets the number of registered actions
     * @return count of registered actions
     */
    public int getActionCount() {
        return actionRegistry.size();
    }
    
    /**
     * Gets the number of registered conditions
     * @return count of registered conditions
     */
    public int getConditionCount() {
        return conditionRegistry.size();
    }
}
