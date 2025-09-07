package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Default implementation of the ScriptEngine interface.
 * Combines the best features of ScriptExecutor and ExecutorEngine
 * with a focus on performance and extensibility.
 * Updated to use ActionFactory and ConditionFactory for dynamic block execution.
 */
public class DefaultScriptEngine implements ScriptEngine {
    
    private final MegaCreative plugin;
    private final VariableManager variableManager;
    private final VisualDebugger debugger;
    private BlockConfigService blockConfigService;
    
    private final ActionFactory actionFactory;
    private final ConditionFactory conditionFactory;
    
    private final Map<BlockType, BlockAction> actionRegistry = new ConcurrentHashMap<>();
    private final Map<BlockType, BlockCondition> conditionRegistry = new ConcurrentHashMap<>();
    private final Map<String, BlockType> blockTypeCache = new ConcurrentHashMap<>();
    
    private static final int MAX_RECURSION_DEPTH = 100;  
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    
    private boolean initialized = false;
    
    public DefaultScriptEngine(MegaCreative plugin, VariableManager variableManager, VisualDebugger debugger, BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        this.debugger = debugger;
        this.blockConfigService = blockConfigService;
        
        // Initialize factories
        this.actionFactory = new ActionFactory(blockConfigService);
        this.conditionFactory = new ConditionFactory(blockConfigService);
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
        blockTypeCache.clear();
        
        // Re-register actions and conditions with the new config service
        if (initialized) {
            registerActionsAndConditions();
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
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        // Register actions and conditions
        registerActionsAndConditions();
        
        initialized = true;
        plugin.getLogger().info("DefaultScriptEngine initialized with " + 
            actionRegistry.size() + " actions and " + 
            conditionRegistry.size() + " conditions");
    }
    
    /**
     * Registers all actions and conditions from configuration
     */
    private void registerActionsAndConditions() {
        actionRegistry.clear();
        conditionRegistry.clear();
        
        if (blockConfigService == null) {
            plugin.getLogger().warning("BlockConfigService is null, cannot register actions and conditions");
            return;
        }
        
        // Register all configured actions
        int actionCount = 0;
        for (BlockConfigService.BlockConfig config : blockConfigService.getAllBlockConfigs()) {
            if ("ACTION".equals(config.getType()) || "EVENT".equals(config.getType())) {
                BlockAction action = actionFactory.createAction(config.getActionName());
                if (action != null) {
                    // Try to get BlockType from enum first, then create dynamic one
                    BlockType blockType = BlockType.getByMaterialAndAction(config.getMaterial(), config.getActionName());
                    if (blockType == null) {
                        // For now, we'll use the enum-based approach for compatibility
                        // In a full refactor, we'd create dynamic BlockType instances
                        blockType = getBlockType(config.getMaterial(), config.getActionName());
                    }
                    
                    if (blockType != null) {
                        actionRegistry.put(blockType, action);
                        actionCount++;
                    }
                }
            }
        }
        
        // Register all configured conditions
        int conditionCount = 0;
        for (BlockConfigService.BlockConfig config : blockConfigService.getAllBlockConfigs()) {
            if ("CONDITION".equals(config.getType()) || "CONTROL".equals(config.getType())) {
                BlockCondition condition = conditionFactory.createCondition(config.getActionName());
                if (condition != null) {
                    // Try to get BlockType from enum first, then create dynamic one
                    BlockType blockType = BlockType.getByMaterialAndAction(config.getMaterial(), config.getActionName());
                    if (blockType == null) {
                        // For now, we'll use the enum-based approach for compatibility
                        // In a full refactor, we'd create dynamic BlockType instances
                        blockType = getBlockType(config.getMaterial(), config.getActionName());
                    }
                    
                    if (blockType != null) {
                        conditionRegistry.put(blockType, condition);
                        conditionCount++;
                    }
                }
            }
        }
        
        plugin.getLogger().info("Registered " + actionCount + " actions and " + conditionCount + " conditions from configuration");
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
                    ExecutionResult result = processBlock(startBlock, context, 0);
                    return result != null ? result : ExecutionResult.success("Script executed successfully");
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
    private ExecutionResult processBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
        if (block == null) {
            return ExecutionResult.failure("Null block provided");
        }
        
        if (context.isCancelled()) {
            return ExecutionResult.failure("Execution was cancelled");
        }

        // Handle pausing for debugging
        if (context.isPaused() && !context.isStepping()) {
            synchronized (context) {
                try {
                    context.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return ExecutionResult.failure("Execution was interrupted");
                }
            }
        }
        
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
            
            // Evaluate the condition with the block and context
            ExecutionResult result = condition.evaluate(block, context);
            
            // Handle the result
            if (result.isSuccess() && "true".equals(result.getMessage())) {
                // Condition is true, execute the next block
                CodeBlock nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    return processBlock(nextBlock, context, recursionDepth + 1);
                }
                return ExecutionResult.success("Condition evaluated to true");
            } else {
                // Condition is false or failed, skip the next block
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
                // In the new system, we might need to create a dynamic BlockType
                // For now, we'll just return null if not found in enum
            }
            
            return blockType;
        });
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
     * Gets the ActionFactory instance
     * @return The ActionFactory instance
     */
    public ActionFactory getActionFactory() {
        return actionFactory;
    }
    
    /**
     * Gets the ConditionFactory instance
     * @return The ConditionFactory instance
     */
    public ConditionFactory getConditionFactory() {
        return conditionFactory;
    }
}