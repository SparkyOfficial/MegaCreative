package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.interfaces.IActionFactory;
import com.megacreative.interfaces.IConditionFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.logging.Logger;

/**
 * A simplified script engine that executes code blocks in a clear, predictable loop.
 * 
 * This implementation focuses on a clean execution flow:
 * 1. Execute the current block
 * 2. Determine the next block based on the result and block type
 * 3. Continue until there are no more blocks or a termination condition is met
 */
public class SimpleScriptEngine implements ScriptEngine {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleScriptEngine.class.getName());
    
    private final MegaCreative plugin;
    private final VariableManager variableManager;
    private final VisualDebugger debugger;
    private final BlockConfigService blockConfigService;
    
    // Active executions map for tracking running scripts
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    
    // Block executors for different block types
    private final Map<BlockType, BlockExecutor> executors = new HashMap<>();
    
    private IActionFactory actionFactory;
    private IConditionFactory conditionFactory;
    
    public SimpleScriptEngine(MegaCreative plugin, VariableManager variableManager, 
                             VisualDebugger debugger, BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        this.debugger = debugger;
        this.blockConfigService = blockConfigService;
        
        // Initialize block executors
        initializeExecutors();
    }
    
    /**
     * Initialize block executors for different block types
     */
    private void initializeExecutors() {
        // Get factories from service registry
        actionFactory = plugin.getServiceRegistry().getActionFactory();
        conditionFactory = plugin.getServiceRegistry().getConditionFactory();
        
        // Register executors for each block type
        executors.put(BlockType.EVENT, new EventBlockExecutor());
        executors.put(BlockType.ACTION, new ActionBlockExecutor((ActionFactory) actionFactory));
        executors.put(BlockType.CONDITION, new ConditionBlockExecutor((ConditionFactory) conditionFactory));
        executors.put(BlockType.CONTROL, new ControlFlowBlockExecutor((ActionFactory) actionFactory, (ConditionFactory) conditionFactory));
        executors.put(BlockType.FUNCTION, new FunctionBlockExecutor());
    }
    
    /**
     * Executes a script starting from the given block
     * 
     * @param startBlock The first block to execute
     * @param context The execution context
     * @return The result of the execution
     */
    public ExecutionResult execute(CodeBlock startBlock, ExecutionContext context) {
        CodeBlock currentBlock = startBlock;
        int instructionLimit = 1000; // Protection against infinite loops
        String executionId = context.getExecutionId();
        
        // Track this execution
        activeExecutions.put(executionId, context);
        
        try {
            while (currentBlock != null && instructionLimit-- > 0) {
                // Check if execution has been cancelled
                if (context.isCancelled()) {
                    return ExecutionResult.error("Execution cancelled");
                }
                
                // Execute the current block
                ExecutionResult result = executeSingleBlock(currentBlock, context);
                
                // Handle special result types
                if (!result.isSuccess()) {
                    LOGGER.warning("Error executing block: " + result.getMessage());
                    return result;
                }
                
                if (result.isTerminated()) {
                    return result;
                }
                
                if (result.isPaused()) {
                    // For paused executions, we return immediately
                    // The caller is responsible for resuming execution
                    return result;
                }
                
                // Determine the next block to execute
                currentBlock = findNextBlock(currentBlock, result, context);
            }
            
            // Check if we exited due to instruction limit
            if (instructionLimit <= 0) {
                return ExecutionResult.error("Script exceeded maximum instruction limit");
            }
            
            return ExecutionResult.success("Script finished successfully");
        } finally {
            // Clean up execution tracking
            activeExecutions.remove(executionId);
        }
    }
    
    /**
     * Executes a single block
     * 
     * @param block The block to execute
     * @param context The execution context
     * @return The result of the execution
     */
    private ExecutionResult executeSingleBlock(CodeBlock block, ExecutionContext context) {
        // Determine block type
        BlockType blockType = determineBlockType(block);
        
        // Get the appropriate executor for this block type
        BlockExecutor executor = executors.get(blockType);
        if (executor == null) {
            return ExecutionResult.error("No executor found for block type: " + blockType);
        }
        
        // Notify debugger if active
        if (debugger.isDebugging(context.getPlayer())) {
            debugger.onBlockExecute(context.getPlayer(), block, null);
        }
        
        try {
            // Execute the block
            return executor.execute(block, context);
        } catch (Exception e) {
            LOGGER.severe("Exception executing block: " + e.getMessage());
            return ExecutionResult.error("Exception executing block: " + e.getMessage());
        }
    }
    
    /**
     * Determines the block type
     */
    private BlockType determineBlockType(CodeBlock block) {
        BlockType blockType = BlockType.ACTION; 
        BlockType type = getBlockType(Material.getMaterial(block.getMaterialName()), block.getAction());
        if (type != null) {
            blockType = type;
        }
        return blockType;
    }
    
    /**
     * Determines the next block to execute based on the current block and result
     * 
     * @param currentBlock The current block that was executed
     * @param lastResult The result of executing the current block
     * @param context The execution context
     * @return The next block to execute, or null if execution should end
     */
    private CodeBlock findNextBlock(CodeBlock currentBlock, ExecutionResult lastResult, ExecutionContext context) {
        // Determine block type
        BlockType blockType = determineBlockType(currentBlock);
        
        // Handle IF blocks (conditions)
        if (blockType == BlockType.CONDITION) {
            // Get the condition result from the execution result
            Object conditionResultObj = lastResult.getDetail("condition_result");
            boolean conditionResult = false;
            if (conditionResultObj instanceof Boolean) {
                conditionResult = (Boolean) conditionResultObj;
            }
            
            if (conditionResult) {
                // If condition is true, go to the first child block (inside IF)
                if (!currentBlock.getChildren().isEmpty()) {
                    return currentBlock.getChildren().get(0);
                }
                // If no children, go to next block
                return currentBlock.getNextBlock();
            } else {
                // If condition is false, look for an ELSE block
                CodeBlock elseBlock = currentBlock.getElseBlock();
                if (elseBlock != null && !elseBlock.getChildren().isEmpty()) {
                    // Go to the first child of the ELSE block
                    return elseBlock.getChildren().get(0);
                }
                // If no ELSE block or children, find the next block after the IF structure
                return findNextAfterScope(currentBlock);
            }
        }
        
        // For all other blocks, just go to the next block in sequence
        return currentBlock.getNextBlock();
    }
    
    /**
     * Finds the next block after a control structure (like IF-ELSE)
     * 
     * @param startBlock The starting block of the control structure
     * @return The next block after the control structure, or null if not found
     */
    private CodeBlock findNextAfterScope(CodeBlock startBlock) {
        // For now, we'll simply return the next block
        // In a more sophisticated implementation, we would need to traverse
        // the block structure to find the end of the control scope
        return startBlock.getNextBlock();
    }
    
    // Implementation of ScriptEngine interface methods
    
    @Override
    public CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, String trigger) {
        // Create execution context
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .build();
        
        // Execute synchronously for now, but wrap in CompletableFuture for API compatibility
        ExecutionResult result = execute(script.getRootBlock(), context);
        return CompletableFuture.completedFuture(result);
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, String trigger) {
        // Create execution context
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .build();
        
        // Execute synchronously for now, but wrap in CompletableFuture for API compatibility
        ExecutionResult result = execute(block, context);
        return CompletableFuture.completedFuture(result);
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlockChain(CodeBlock startBlock, Player player, String trigger) {
        // For simplicity, we'll just execute the single block
        // In a more complete implementation, this would execute the entire chain
        return executeBlock(startBlock, player, trigger);
    }
    
    @Override
    public void registerAction(BlockType type, BlockAction action) {
        // Not implemented in this simplified version
        LOGGER.fine("Action registration via ScriptEngine is deprecated. Use @BlockMeta annotation instead.");
    }
    
    @Override
    public void registerCondition(BlockType type, BlockCondition condition) {
        // Not implemented in this simplified version
        LOGGER.fine("Condition registration via ScriptEngine is deprecated. Use @BlockMeta annotation instead.");
    }
    
    @Override
    public BlockType getBlockType(Material material, String actionName) {
        if (material != null && actionName != null) {
            // Try to find block type from block config service
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(actionName);
            if (config != null) {
                return BlockType.fromString(config.getType());
            }
        }
        return BlockType.ACTION; // Default fallback
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
        if (context != null) {
            context.setPaused(false);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean stepExecution(String executionId) {
        // Not implemented in this simplified version
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
}