package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.services.BlockConfigService;
import com.megacreative.core.DependencyContainer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
    private final DependencyContainer dependencyContainer;
    
    private final ActionFactory actionFactory;
    private final ConditionFactory conditionFactory;
    
    private final Map<BlockType, BlockAction> actionRegistry = new ConcurrentHashMap<>();
    private final Map<BlockType, BlockCondition> conditionRegistry = new ConcurrentHashMap<>();
    private final Map<String, BlockType> blockTypeCache = new ConcurrentHashMap<>();
    
    private static final int MAX_RECURSION_DEPTH = 100;  
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    
    private boolean initialized = false;
    
    public DefaultScriptEngine(MegaCreative plugin, VariableManager variableManager, VisualDebugger debugger, BlockConfigService blockConfigService, DependencyContainer dependencyContainer) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        this.debugger = debugger;
        this.blockConfigService = blockConfigService;
        this.dependencyContainer = dependencyContainer;
        
        // Initialize factories with dependency container
        this.actionFactory = new ActionFactory(dependencyContainer);
        this.conditionFactory = new ConditionFactory();
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
                BlockAction action = actionFactory.createAction(config.getId());
                if (action != null) {
                    // Create a dynamic BlockType based on the config
                    BlockType blockType = BlockType.fromString(config.getType());
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
                BlockCondition condition = conditionFactory.createCondition(config.getId());
                if (condition != null) {
                    // Create a dynamic BlockType based on the config
                    BlockType blockType = BlockType.fromString(config.getType());
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
        if (script == null || script.getRootBlock() == null) {
            return CompletableFuture.completedFuture(ExecutionResult.error("Invalid script or empty root block"));
        }
        
        String executionId = UUID.randomUUID().toString();
        ExecutionContext context = new ExecutionContext(
            plugin, 
            player, 
            player != null ? plugin.getWorldManager().getWorld(player.getWorld().getName()) : null,
            null, // event
            null, // blockLocation
            script.getRootBlock()
        );
        
        activeExecutions.put(executionId, context);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Process the starting block
                CodeBlock startBlock = script.getRootBlock();
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
    public CompletableFuture<ExecutionResult> executeBlockChain(CodeBlock startBlock, Player player, String trigger) {
         if (startBlock == null) {
             return CompletableFuture.completedFuture(ExecutionResult.error("Start block is null"));
         }
         
         String executionId = UUID.randomUUID().toString();
         ExecutionContext context = new ExecutionContext(
             plugin, 
             player, 
             player != null ? plugin.getWorldManager().getWorld(player.getWorld().getName()) : null,
             null, // event
             null, // blockLocation
             startBlock
         );
         
         activeExecutions.put(executionId, context);
         
         return CompletableFuture.supplyAsync(() -> {
             try {
                 ExecutionResult result = processBlock(startBlock, context, 0);
                 return result != null ? result : ExecutionResult.success("Block chain executed successfully");
             } catch (Exception e) {
                 plugin.getLogger().severe("Error executing block chain: " + e.getMessage());
                 e.printStackTrace();
                 return ExecutionResult.error("Error executing block chain: " + e.getMessage());
             } finally {
                 activeExecutions.remove(executionId);
             }
         });
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, String trigger) {
        if (block == null) {
            return CompletableFuture.completedFuture(ExecutionResult.error("Invalid block provided"));
        }
        
        String executionId = UUID.randomUUID().toString();
        ExecutionContext context = new ExecutionContext(
            plugin, 
            player, 
            player != null ? plugin.getWorldManager().getWorld(player.getWorld().getName()) : null,
            null, // event
            null, // blockLocation
            block
        );
        
        activeExecutions.put(executionId, context);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                ExecutionResult result = processBlock(block, context, 0);
                return result != null ? result : ExecutionResult.success("Block executed successfully");
            } catch (Exception e) {
                plugin.getLogger().severe("Error executing block: " + e.getMessage());
                e.printStackTrace();
                return ExecutionResult.error("Error executing block: " + e.getMessage());
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
            return ExecutionResult.error("Null block provided");
        }
        
        if (context.isCancelled()) {
            return ExecutionResult.error("Execution was cancelled");
        }

        // Handle pausing for debugging
        if (context.isPaused() && !context.isStepping()) {
            synchronized (context) {
                try {
                    context.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return ExecutionResult.error("Execution was interrupted");
                }
            }
        }
        
        try {
            // Check recursion depth
            if (recursionDepth > MAX_RECURSION_DEPTH) {
                return ExecutionResult.error("Maximum recursion depth (" + MAX_RECURSION_DEPTH + ") exceeded");
            }
            
            // Get block type using the new mapping system
            BlockType blockType = getBlockType(block.getMaterial(), block.getAction());
            if (blockType == null) {
                return ExecutionResult.error("Unknown block type: " + block.getMaterial() + "/" + block.getAction());
            }
            
            // Handle conditions and actions differently
            BlockAction action = actionRegistry.get(blockType);
            BlockCondition condition = conditionRegistry.get(blockType);
            
            if (action != null) {
                // Execute action block
                return handleAction(block, context, recursionDepth);
            } else if (condition != null) {
                // Handle condition block
                return handleCondition(block, context, recursionDepth);
            } else {
                return ExecutionResult.error("No handler registered for block type: " + blockType);
            }
        } catch (Exception e) {
            String errorMsg = "Error processing block: " + e.getMessage();
            plugin.getLogger().severe(errorMsg);
            
            // Visual feedback for error
            if (context.getPlayer() != null && debugger != null) {
                debugger.showError(context.getPlayer(), context.getBlockLocation(), errorMsg);
            }

            return ExecutionResult.error(errorMsg);
        }
    }
    
    private ExecutionResult handleAction(CodeBlock block, ExecutionContext context, int recursionDepth) {
        try {
            // Get the block type
            BlockType blockType = getBlockType(block.getMaterial(), block.getAction());
            if (blockType == null) {
                return ExecutionResult.error("Unknown block type: " + block.getMaterial() + "/" + block.getAction());
            }
            
            // Get the action handler
            BlockAction action = actionRegistry.get(blockType);
            if (action == null) {
                return ExecutionResult.error("No action handler registered for block type: " + blockType);
            }
            
            // Visual feedback for debugging
            if (context.getPlayer() != null && debugger != null) {
                debugger.onBlockExecute(context.getPlayer(), block, block.getLocation());
            }
            
            // Execute the action with the block and context
            ExecutionResult result = action.execute(block, context);
            
            // Handle the result
            if (result.isSuccess()) {
                // Action succeeded, execute the next block
                CodeBlock nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    return processBlock(nextBlock, context, recursionDepth + 1);
                }
                return ExecutionResult.success("Action executed successfully");
            } else {
                // Action failed, stop execution and report error
                String errorMsg = "Action failed: " + result.getMessage();
                plugin.getLogger().severe(errorMsg);
                
                // Visual feedback for error
                if (context.getPlayer() != null && debugger != null) {
                    debugger.showError(context.getPlayer(), block.getLocation(), errorMsg);
                }

                return ExecutionResult.error(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error executing action: " + e.getMessage();
            plugin.getLogger().severe(errorMsg);
            
            // Visual feedback for error
            if (context.getPlayer() != null && debugger != null) {
                debugger.showError(context.getPlayer(), block.getLocation(), errorMsg);
            }

            return ExecutionResult.error(errorMsg);
        }
    }
    
    private ExecutionResult handleCondition(CodeBlock block, ExecutionContext context, int recursionDepth) {
        try {
            // Get the block type
            BlockType blockType = getBlockType(block.getMaterial(), block.getAction());
            if (blockType == null) {
                return ExecutionResult.error("Unknown block type: " + block.getMaterial() + "/" + block.getAction());
            }
            
            // Get the condition handler
            BlockCondition condition = conditionRegistry.get(blockType);
            if (condition == null) {
                return ExecutionResult.error("No condition handler registered for block type: " + blockType);
            }
            
            // Visual feedback for debugging
            if (context.getPlayer() != null && debugger != null) {
                debugger.onBlockExecute(context.getPlayer(), block, block.getLocation());
            }
            
            // Special handling for else blocks
            if (block.getAction().equals("else")) {
                // Execute else block only if the previous condition was false
                if (!context.getLastConditionResult()) {
                    CodeBlock nextBlock = block.getNextBlock();
                    if (nextBlock != null) {
                        return processBlock(nextBlock, context, recursionDepth + 1);
                    }
                    return ExecutionResult.success("Else block executed");
                } else {
                    // Previous condition was true, skip the else block
                    CodeBlock nextBlock = findNextBlockAfterElse(block);
                    if (nextBlock != null) {
                        return processBlock(nextBlock, context, recursionDepth + 1);
                    }
                    return ExecutionResult.success("Else block skipped");
                }
            }
            
            // Evaluate the condition with the block and context
            boolean conditionResult = condition.evaluate(block, context);
            
            // Store the result for potential else block handling
            context.setLastConditionResult(conditionResult);
            
            // Visual feedback for debugging
            if (context.getPlayer() != null && debugger != null) {
                debugger.onConditionResult(context.getPlayer(), block, conditionResult);
            }

            // Handle the result based on the condition evaluation
            if (conditionResult) {
                // Condition is true, execute the next block
                CodeBlock nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    return processBlock(nextBlock, context, recursionDepth + 1);
                }
                return ExecutionResult.success("Condition evaluated to true");
            } else {
                // Condition is false, find the next appropriate block in the chain
                CodeBlock nextBlock = findNextBlockInChain(block);
                if (nextBlock != null) {
                    return processBlock(nextBlock, context, recursionDepth + 1);
                }
                return ExecutionResult.success("Condition evaluated to false");
            }
        } catch (Exception e) {
            String errorMsg = "Error evaluating condition: " + e.getMessage();
            plugin.getLogger().severe(errorMsg);
            
            // Visual feedback for error
            if (context.getPlayer() != null && debugger != null) {
                debugger.showError(context.getPlayer(), block.getLocation(), errorMsg);
            }
            
            return ExecutionResult.error(errorMsg);
        }
    }
    
    /**
     * Finds the next appropriate block in a conditional chain
     * @param conditionBlock The current condition block
     * @return The next block to execute, or null if not found
     */
    private CodeBlock findNextBlockInChain(CodeBlock conditionBlock) {
        // Look for else or else if blocks following this condition
        CodeBlock current = conditionBlock.getNextBlock();
        
        // Skip blocks until we find an else block or reach the end of the chain
        while (current != null) {
            // If we find an else block, execute it
            if ("else".equals(current.getAction())) {
                return current;
            }
            // If we find another condition that's not an else, skip it
            // (this would be an else-if in a real implementation)
            current = current.getNextBlock();
        }
        
        // If no else block is found, continue with the next block after the chain
        return conditionBlock.getNextBlock();
    }
    
    /**
     * Finds the next block after an else block
     * @param elseBlock The else block
     * @return The next block to execute, or null if not found
     */
    private CodeBlock findNextBlockAfterElse(CodeBlock elseBlock) {
        // Simply return the next block after the else block
        return elseBlock.getNextBlock();
    }

    @Override
    public BlockType getBlockType(Material material, String actionName) {
        if (material == null || actionName == null) {
            return null;
        }
        
        String cacheKey = material.name() + ":" + actionName;
        return blockTypeCache.computeIfAbsent(cacheKey, k -> {
            // In the new system, we determine BlockType from the configuration
            if (blockConfigService != null) {
                BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(actionName);
                if (config != null) {
                    return BlockType.fromString(config.getType());
                }
            }
            
            // Default fallback
            return BlockType.UNKNOWN;
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