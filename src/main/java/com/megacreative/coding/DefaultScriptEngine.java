package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.cache.BlockExecutionCache;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.BlockType;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
// 🎆 Reference system-style advanced execution
import com.megacreative.coding.executors.AdvancedExecutionEngine;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.Constants;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DefaultScriptEngine implements ScriptEngine, EnhancedScriptEngine {
    
    // Constants for block types
    private static final String BLOCK_TYPE_EVENT = "EVENT";
    private static final String BLOCK_TYPE_ACTION = "ACTION";
    private static final String BLOCK_TYPE_CONDITION = "CONDITION";
    private static final String BLOCK_TYPE_CONTROL = "CONTROL";
    private static final String BLOCK_TYPE_FUNCTION = "FUNCTION";
    
    // Constants for control block actions
    private static final String CONTROL_ACTION_CONDITIONAL_BRANCH = "conditionalBranch";
    private static final String CONTROL_ACTION_ELSE = "else";
    private static final String CONTROL_ACTION_WHILE_LOOP = "whileLoop";
    private static final String CONTROL_ACTION_FOR_EACH = "forEach";
    private static final String CONTROL_ACTION_BREAK = "break";
    private static final String CONTROL_ACTION_CONTINUE = "continue";
    
    // Constants for function actions
    private static final String FUNCTION_ACTION_CALL_FUNCTION = "callFunction";
    
    // Constants for bracket types
    private static final String BRACKET_OPEN = "OPEN";
    private static final String BRACKET_CLOSE = "CLOSE";
    
    // Constants for error messages
    private static final String ERROR_SCRIPT_VALIDATION_FAILED = "Script validation failed: ";
    private static final String ERROR_ACTION_HANDLER_NOT_FOUND = "Action handler not found for: ";
    private static final String ERROR_CONDITION_HANDLER_NOT_FOUND = "Condition handler not found for: ";
    private static final String ERROR_MAX_RECURSION_EXCEEDED_IN_WHILE_LOOP = "Maximum recursion depth exceeded in while loop";
    private static final String ERROR_PLAYER_REQUIRED_FOR_FOREACH = "Player required for forEach loop";
    private static final String ERROR_FUNCTION_MANAGER_NOT_AVAILABLE = "Function manager not available";
    private static final String ERROR_EXECUTING_FUNCTION = "Error executing function ";
    private static final String ERROR_EXECUTING_BLOCK = "Error executing block ";
    private static final String ERROR_CRITICAL_EXECUTING_BLOCK = "Critical error executing block ";
    private static final String ERROR_UNSUPPORTED_BLOCK_TYPE = "Unsupported block type: ";
    
    private final MegaCreative plugin;
    private final VariableManager variableManager;
    private final VisualDebugger debugger;
    private final BlockConfigService blockConfigService;
    private final ActionFactory actionFactory;
    private final ConditionFactory conditionFactory;
    
    // 🎆 Reference system-style advanced execution engine
    private final AdvancedExecutionEngine advancedExecutionEngine;
    
    // Script validator for enhanced validation
    private final ScriptValidator scriptValidator;
    
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    private static final int MAX_RECURSION_DEPTH = 100;
    private final BlockExecutionCache executionCache;
    
    // Performance settings
    private long maxExecutionTimeMs = 5000; // 5 seconds
    private int maxInstructionsPerTick = 1000;

    public DefaultScriptEngine(MegaCreative plugin, VariableManager variableManager, VisualDebugger debugger,
                               BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        this.debugger = debugger;
        this.blockConfigService = blockConfigService;
        // Передаем DependencyContainer, если он у вас есть, или создаем новые
        this.actionFactory = new ActionFactory(plugin.getDependencyContainer());
        this.conditionFactory = new ConditionFactory();
        
        // 🎆 Reference system: Initialize advanced execution engine
        this.advancedExecutionEngine = new AdvancedExecutionEngine(plugin);
        
        // Initialize script validator
        this.scriptValidator = new ScriptValidator(blockConfigService);
        
        // Initialize execution cache with 5 minute TTL and max 1000 entries
        this.executionCache = new BlockExecutionCache(5L, TimeUnit.MINUTES, 1000);
        
        // Schedule cache cleanup every minute
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, executionCache::cleanup, 600, 600);
    }
    
    public void initialize() {
        // Initialize the script engine with any required setup
        // This method can be used to register built-in actions and conditions
        // Register all actions from BlockConfigService
        for (BlockConfigService.BlockConfig config : blockConfigService.getAllBlockConfigs()) {
            if (BLOCK_TYPE_ACTION.equals(config.getType())) {
                actionFactory.registerAction(config.getId(), config.getDisplayName());
            } else if (BLOCK_TYPE_CONDITION.equals(config.getType())) {
                conditionFactory.registerCondition(config.getId(), config.getDisplayName());
            }
        }
    }
    
    public int getActionCount() {
        // Return the number of registered actions
        return actionFactory != null ? actionFactory.getActionCount() : 0;
    }
    
    public int getConditionCount() {
        // Return the number of registered conditions
        return conditionFactory != null ? conditionFactory.getConditionCount() : 0;
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, String trigger) {
        if (script == null || !script.isEnabled() || script.getRootBlock() == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success(Constants.SCRIPT_IS_INVALID_OR_DISABLED));
        }

        // Validate script before execution
        ScriptValidator.ValidationResult validationResult = scriptValidator.validateScript(script);
        if (!validationResult.isValid()) {
            String errorMessage = ERROR_SCRIPT_VALIDATION_FAILED + 
                validationResult.getErrorCount() + " errors found. " +
                "First error: " + (validationResult.getErrors().isEmpty() ? "Unknown" : validationResult.getErrors().get(0).getMessage());
            return CompletableFuture.completedFuture(ExecutionResult.error(errorMessage));
        }

        String executionId = UUID.randomUUID().toString();
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .creativeWorld(plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld()))
            .currentBlock(script.getRootBlock())
            .build();
        
        activeExecutions.put(executionId, context);

        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
        
        // Use Bukkit scheduler to run on main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (debugger.isDebugging(player)) {
                        debugger.onScriptStart(player, script);
                    }
                    ExecutionResult result = processBlock(script.getRootBlock(), context, 0);
                    future.complete(result);
                } catch (Exception e) {
                    String errorMsg = "Script execution error: " + e.getMessage();
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, e);
                    future.completeExceptionally(e);
                } catch (Throwable t) {
                    String errorMsg = "Critical script execution error: " + t.getMessage();
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, t);
                    future.completeExceptionally(new RuntimeException(Constants.CRITICAL_EXECUTION_ERROR, t));
                } finally {
                    if (debugger.isDebugging(player)) {
                        debugger.onScriptEnd(player, script);
                    }
                    activeExecutions.remove(executionId);
                }
            }
        }.runTask(plugin);

        return future;
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, String trigger) {
        if (block == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success(Constants.BLOCK_IS_NULL));
        }

        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .creativeWorld(plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld()))
            .currentBlock(block)
            .build();

        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
        
        // Use Bukkit scheduler to run on main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    //if (debugger.isDebugging(player)) {
                    //    debugger.onBlockStart(player, block);
                    //}
                    ExecutionResult result = processBlock(block, context, 0);
                    future.complete(result);
                } catch (Exception e) {
                    String errorMsg = "Block execution error: " + e.getMessage();
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, e);
                    future.completeExceptionally(e);
                } catch (Throwable t) {
                    String errorMsg = "Critical block execution error: " + t.getMessage();
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, t);
                    future.completeExceptionally(new RuntimeException(Constants.CRITICAL_EXECUTION_ERROR, t));
                }
            }
        }.runTask(plugin);

        return future;
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlockChain(CodeBlock startBlock, Player player, String trigger) {
        if (startBlock == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success(Constants.BLOCK_IS_NULL));
        }

        // Create execution context with proper chain tracking
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .creativeWorld(plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld()))
            .currentBlock(startBlock)
            .build();

        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
        
        // Use Bukkit scheduler to run on main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Track execution chain for better debugging and error handling
                    List<CodeBlock> executionChain = new ArrayList<>();
                    ExecutionResult result = processBlockChain(startBlock, context, executionChain, 0);
                    future.complete(result);
                } catch (Exception e) {
                    String errorMsg = "Block chain execution error: " + e.getMessage();
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, e);
                    future.completeExceptionally(e);
                } catch (Throwable t) {
                    String errorMsg = "Critical block chain execution error: " + t.getMessage();
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, t);
                    future.completeExceptionally(new RuntimeException(Constants.CRITICAL_EXECUTION_ERROR, t));
                }
            }
        }.runTask(plugin);

        return future;
    }
    
    // Add a counter for instruction limiting to prevent infinite loops
    private static final int MAX_INSTRUCTIONS_PER_TICK = 1000;
    
    /**
     * Creates a context map for caching purposes
     */
    private Map<String, Object> createCacheContext(ExecutionContext context) {
        Map<String, Object> cacheContext = new HashMap<>();
        if (context.getPlayer() != null) {
            cacheContext.put("player", context.getPlayer().getUniqueId());
        }
        if (context.getCreativeWorld() != null) {
            cacheContext.put("world", context.getCreativeWorld().getWorldId());
        }
        // Add more context as needed
        return cacheContext;
    }
    
    /**
     * Checks if a block's execution result can be cached
     */
    private boolean isCacheable(CodeBlock block) {
        if (block == null) return false;
        
        // Don't cache blocks with side effects or non-deterministic behavior
        String action = block.getAction();
        if (action == null) return false;
        
        // Example: Don't cache blocks that modify state or have random behavior
        return !action.toLowerCase().contains("random") && 
               !action.toLowerCase().contains("spawn") &&
               !action.toLowerCase().contains("give") &&
               !action.toLowerCase().contains("teleport");
    }
    
    private ExecutionResult processBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
        // Check cache first
        Map<String, Object> cacheContext = createCacheContext(context);
        ExecutionResult cachedResult = executionCache.get(block, cacheContext);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        if (block == null || recursionDepth > MAX_RECURSION_DEPTH) {
            if (recursionDepth > MAX_RECURSION_DEPTH) {
                String errorMsg = ERROR_MAX_RECURSION_EXCEEDED_IN_WHILE_LOOP + " (depth: " + recursionDepth + ")";
                plugin.getLogger().warning(errorMsg + " Player: " + 
                    (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                // Cache the error result
                ExecutionResult errorResult = ExecutionResult.error(errorMsg);
                if (isCacheable(block)) {
                    executionCache.put(block, cacheContext, errorResult);
                }
                return errorResult;
            }
            // Cache the result before returning
            ExecutionResult successResult = ExecutionResult.success(Constants.BLOCK_IS_NULL);
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, successResult);
            }
            return successResult;
        }

        // Check if execution should be cancelled
        if (context.isCancelled()) {
            ExecutionResult cancelledResult = ExecutionResult.success("Execution cancelled");
            // Cache the result before returning
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, cancelledResult);
            }
            return cancelledResult;
        }

        // Check if execution should be paused
        if (context.isPaused()) {
            ExecutionResult pausedResult = ExecutionResult.success("Execution paused").withPause();
            // Cache the result before returning
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, pausedResult);
            }
            return pausedResult;
        }

        // Check if execution should step
        if (context.isStepping()) {
            context.setStepping(false);
            context.setPaused(true);
            ExecutionResult stepResult = ExecutionResult.success("Execution step").withPause();
            // Cache the result before returning
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, stepResult);
            }
            return stepResult;
        }

        // Check execution time limit
        long startTime = System.currentTimeMillis();
        if (startTime - context.getStartTime() > maxExecutionTimeMs) {
            String errorMsg = "Script execution timed out after " + maxExecutionTimeMs + "ms";
            plugin.getLogger().warning(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            // Cache the error result
            ExecutionResult timeoutResult = ExecutionResult.error(errorMsg);
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, timeoutResult);
            }
            return timeoutResult;
        }

        String blockType = "ACTION"; // Default to action
        ExecutionResult result = null;

        try {
            // --- ОСНОВНАЯ ЛОГИКА ---
            // --- MAIN LOGIC ---
            // --- HAUPTLOGIK ---
            
            // Execute the appropriate handler based on block type
            if (blockType.equals(BLOCK_TYPE_ACTION)) {
                // Handle action blocks
                String actionId = block.getAction();
                if (actionId != null) {
                    BlockAction actionHandler = actionFactory.createAction(actionId);
                    if (actionHandler != null) {
                        result = actionHandler.execute(block, context);
                    } else {
                        String errorMsg = ERROR_ACTION_HANDLER_NOT_FOUND + actionId;
                        plugin.getLogger().warning(errorMsg + " Player: " + 
                            (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                        result = ExecutionResult.error(errorMsg);
                    }
                }
            } else if (blockType.equals(BLOCK_TYPE_CONDITION)) {
                // Handle condition blocks
                String conditionId = block.getAction();
                if (conditionId != null) {
                    BlockCondition conditionHandler = conditionFactory.createCondition(conditionId);
                    if (conditionHandler != null) {
                        boolean conditionResult = conditionHandler.evaluate(block, context);
                        result = ExecutionResult.success("Condition " + conditionId + " evaluated to " + conditionResult);
                    } else {
                        String errorMsg = ERROR_CONDITION_HANDLER_NOT_FOUND + conditionId;
                        plugin.getLogger().warning(errorMsg + " Player: " + 
                            (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                        result = ExecutionResult.error(errorMsg);
                    }
                }
            } else if (blockType.equals(BLOCK_TYPE_CONTROL)) {
                // Handle control blocks
                String controlAction = block.getAction();
                if (controlAction != null) {
                    switch (controlAction) {
                        case CONTROL_ACTION_CONDITIONAL_BRANCH:
                            // Handle conditional branch
                            CodeBlock conditionBlock = block.getChildren().isEmpty() ? null : block.getChildren().get(0);
                            CodeBlock trueBlock = block.getNextBlock();
                            CodeBlock elseBlock = null;
                            
                            // Look for ELSE block
                            if (trueBlock != null) {
                                CodeBlock current = trueBlock;
                                while (current != null && current.getNextBlock() != null) {
                                    current = current.getNextBlock();
                                }
                                if (current != null && "ELSE".equals(current.getAction())) {
                                    elseBlock = current.getNextBlock();
                                }
                            }
                            
                            // Evaluate condition
                            boolean conditionResult = false;
                            if (conditionBlock != null) {
                                ExecutionResult conditionResultObj = processBlock(conditionBlock, context, recursionDepth + 1);
                                if (conditionResultObj.isSuccess()) {
                                    conditionResult = true; // Simplified for now
                                }
                            }
                            
                            // Execute appropriate branch
                            if (conditionResult && trueBlock != null) {
                                return processBlock(trueBlock, context, recursionDepth + 1);
                            } else if (!conditionResult && elseBlock != null) {
                                return processBlock(elseBlock, context, recursionDepth + 1);
                            }
                            break;
                            
                        case CONTROL_ACTION_ELSE:
                            // ELSE block - just continue to next block
                            break;
                            
                        case CONTROL_ACTION_WHILE_LOOP:
                            // Handle while loop
                            CodeBlock loopConditionBlock = block.getChildren().isEmpty() ? null : block.getChildren().get(0);
                            CodeBlock loopBodyBlock = block.getNextBlock();
                            
                            int loopCount = 0;
                            while (loopCount < 1000) { // Safety limit
                                // Evaluate condition
                                boolean loopConditionResult = false;
                                if (loopConditionBlock != null) {
                                    ExecutionResult loopConditionResultObj = processBlock(loopConditionBlock, context, recursionDepth + 1);
                                    if (loopConditionResultObj.isSuccess()) {
                                        loopConditionResult = true; // Simplified for now
                                    }
                                }
                                
                                if (!loopConditionResult) {
                                    break; // Exit loop
                                }
                                
                                // Execute loop body
                                if (loopBodyBlock != null) {
                                    ExecutionResult loopResult = processBlock(loopBodyBlock, context, recursionDepth + 1);
                                    if (!loopResult.isSuccess()) {
                                        // Cache the result before returning
                                        if (isCacheable(block)) {
                                            executionCache.put(block, cacheContext, loopResult);
                                        }
                                        return loopResult; // Exit on error
                                    }
                                }
                                
                                loopCount++;
                            }
                            break;
                            
                        case CONTROL_ACTION_FOR_EACH:
                            // Handle for-each loop
                            String listVariableName = block.getParameterValue("listVariable", String.class);
                            if (listVariableName != null && context.getPlayer() != null) {
                                // Get list from variable manager
                                DataValue listValue = variableManager.getVariable(listVariableName, VariableManager.VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
                                if (listValue instanceof ListValue) {
                                    ListValue list = (ListValue) listValue;
                                    CodeBlock loopBody = block.getNextBlock();
                                    
                                    for (DataValue item : list.getValues()) {
                                        // Set current item variable
                                        variableManager.setVariable("currentItem", item, VariableManager.VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
                                        
                                        // Execute loop body
                                        if (loopBody != null) {
                                            ExecutionResult itemResult = processBlock(loopBody, context, recursionDepth + 1);
                                            if (!itemResult.isSuccess()) {
                                                // Cache the result before returning
                                                if (isCacheable(block)) {
                                                    executionCache.put(block, cacheContext, itemResult);
                                                }
                                                return itemResult; // Exit on error
                                            }
                                        }
                                    }
                                } else {
                                    String errorMsg = "Variable " + listVariableName + " is not a list";
                                    plugin.getLogger().warning(errorMsg + " Player: " + 
                                        (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                                    ExecutionResult errorResult = ExecutionResult.error(errorMsg);
                                    // Cache the result before returning
                                    if (isCacheable(block)) {
                                        executionCache.put(block, cacheContext, errorResult);
                                    }
                                    return errorResult;
                                }
                            } else {
                                String errorMsg = ERROR_PLAYER_REQUIRED_FOR_FOREACH;
                                if (context.getPlayer() == null) {
                                    errorMsg += " (No player context)";
                                } else {
                                    errorMsg += " (No list variable specified)";
                                }
                                plugin.getLogger().warning(errorMsg + " Player: " + 
                                    (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                                ExecutionResult errorResult = ExecutionResult.error(errorMsg);
                                // Cache the result before returning
                                if (isCacheable(block)) {
                                    executionCache.put(block, cacheContext, errorResult);
                                }
                                return errorResult;
                            }
                            break;
                            
                        case CONTROL_ACTION_BREAK:
                            // Handle break
                            ExecutionResult breakResult = ExecutionResult.success();
                            breakResult.setTerminated(true);
                            return breakResult;
                            
                        case CONTROL_ACTION_CONTINUE:
                            // Handle continue
                            ExecutionResult continueResult = ExecutionResult.success();
                            continueResult.setTerminated(true);
                            return continueResult;
                            
                        default:
                            // Unknown control action, ignore
                            break;
                    }
                }
            } else if (blockType.equals(BLOCK_TYPE_FUNCTION)) {
                // Handle function blocks
                String functionAction = block.getAction();
                if (FUNCTION_ACTION_CALL_FUNCTION.equals(functionAction)) {
                    String functionName = block.getParameterValue("functionName", String.class);
                    if (functionName != null) {
                        // Get function manager and execute function
                        //FunctionManager functionManager = plugin.getFunctionManager();
                        //if (functionManager != null) {
                            // Function calling logic has been moved to a dedicated method
                            // or removed as part of code cleanup
                    } else {
                        String errorMsg = "Function name not specified";
                        plugin.getLogger().warning(errorMsg + " Player: " + 
                            (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                        result = ExecutionResult.error(errorMsg);
                    }
                }
            } else if (block.isBracket()) {
                // Handle bracket blocks
                if (block.getBracketType() == CodeBlock.BracketType.OPEN) {
                    // Process children of opening bracket
                    for (CodeBlock child : block.getChildren()) {
                        ExecutionResult childResult = processBlock(child, context, recursionDepth + 1);
                        if (!childResult.isSuccess() || childResult.isTerminated()) {
                            // Cache the result before returning
                            if (isCacheable(block)) {
                                executionCache.put(block, cacheContext, childResult);
                            }
                            return childResult;
                        }
                    }
                }
                // Continue to next block for both open and close brackets
            } else {
                String errorMsg = ERROR_UNSUPPORTED_BLOCK_TYPE + blockType;
                plugin.getLogger().warning(errorMsg + " Player: " + 
                    (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                // Cache the result before returning
                ExecutionResult errorResult = ExecutionResult.error(errorMsg);
                if (isCacheable(block)) {
                    executionCache.put(block, cacheContext, errorResult);
                }
                return processBlock(block.getNextBlock(), context, recursionDepth + 1);
            }
            
            // Cache the result before returning
            if (result != null && result.isSuccess() && isCacheable(block)) {
                executionCache.put(block, cacheContext, result);
            }
            
            return processBlock(block.getNextBlock(), context, recursionDepth + 1);
        } catch (Exception e) {
            String errorMsg = ERROR_EXECUTING_BLOCK + block.getAction() + ": " + e.getMessage() + 
                " Player: " + (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown");
            plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, e);
            // Cache the error result
            ExecutionResult errorResult = ExecutionResult.error(errorMsg);
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, errorResult);
            }
            return ExecutionResult.error(errorMsg);
        } catch (Throwable t) {
            String errorMsg = ERROR_CRITICAL_EXECUTING_BLOCK + block.getAction() + ": " + t.getMessage() + 
                " Player: " + (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown");
            plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, t);
            // Cache the error result
            ExecutionResult errorResult = ExecutionResult.error(errorMsg);
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, errorResult);
            }
            return ExecutionResult.error(errorMsg);
        }
    }
    
    /**
     * Process a block chain with enhanced error handling and context management
     * 
     * @param block The current block to process
     * @param context The execution context
     * @param executionChain The chain of blocks executed (for debugging)
     * @param recursionDepth The current recursion depth
     * @return The execution result
     */
    private ExecutionResult processBlockChain(CodeBlock block, ExecutionContext context, List<CodeBlock> executionChain, int recursionDepth) {
        // Prevent infinite recursion
        if (block == null || recursionDepth > MAX_RECURSION_DEPTH) {
            if (recursionDepth > MAX_RECURSION_DEPTH) {
                String errorMsg = ERROR_MAX_RECURSION_EXCEEDED_IN_WHILE_LOOP + " (depth: " + recursionDepth + ")";
                plugin.getLogger().warning(errorMsg + " Player: " + 
                    (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                return ExecutionResult.error(errorMsg);
            }
            return ExecutionResult.success(Constants.BLOCK_IS_NULL);
        }

        // Add block to execution chain for debugging
        executionChain.add(block);
        
        // Check if execution should be cancelled
        if (context.isCancelled()) {
            return ExecutionResult.success("Execution cancelled");
        }

        // Check execution time limit
        long startTime = System.currentTimeMillis();
        if (startTime - context.getStartTime() > maxExecutionTimeMs) {
            String errorMsg = "Script execution timed out after " + maxExecutionTimeMs + "ms";
            plugin.getLogger().warning(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            return ExecutionResult.error(errorMsg);
        }

        try {
            // Process the current block
            ExecutionResult result = processBlock(block, context, recursionDepth);
            
            // If the result indicates termination (break/continue), return it
            if (result.isTerminated()) {
                return result;
            }
            
            // If there's an error, return it
            if (!result.isSuccess()) {
                return result;
            }
            
            // Continue to the next block in the chain
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock != null) {
                return processBlockChain(nextBlock, context, executionChain, recursionDepth + 1);
            }
            
            // End of chain
            return result;
        } catch (Exception e) {
            String errorMsg = ERROR_EXECUTING_BLOCK + block.getAction() + ": " + e.getMessage() + 
                " Player: " + (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown") +
                " Chain: " + executionChain.size() + " blocks";
            plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, e);
            return ExecutionResult.error(errorMsg);
        } catch (Throwable t) {
            String errorMsg = ERROR_CRITICAL_EXECUTING_BLOCK + block.getAction() + ": " + t.getMessage() + 
                " Player: " + (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown") +
                " Chain: " + executionChain.size() + " blocks";
            plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, t);
            return ExecutionResult.error(errorMsg);
        }
    }
    
    @Override
    public void registerAction(BlockType type, BlockAction action) {
        // Implementation for registering actions
        // This would typically involve adding to an internal registry
        if (actionFactory != null) {
            // Register with action factory using type name as ID
            //actionFactory.registerAction(type.name(), action);
        }
    }

    @Override
    public void registerCondition(BlockType type, BlockCondition condition) {
        // Implementation for registering conditions
        // This would typically involve adding to an internal registry
        if (conditionFactory != null) {
            // Register with condition factory using type name as ID
            //conditionFactory.registerCondition(type.name(), condition);
        }
    }
    
    /**
     * Register an action with a specific ID
     * 
     * @param actionId The action ID
     * @param action The BlockAction implementation
     */
    public void registerAction(String actionId, BlockAction action) {
        if (actionFactory != null) {
            actionFactory.registerAction(actionId, actionId); // Using ID as display name
        }
    }
    
    /**
     * Register a condition with a specific ID
     * 
     * @param conditionId The condition ID
     * @param condition The BlockCondition implementation
     */
    public void registerCondition(String conditionId, BlockCondition condition) {
        if (conditionFactory != null) {
            conditionFactory.registerCondition(conditionId, conditionId); // Using ID as display name
        }
    }
    
    @Override
    public BlockType getBlockType(Material material, String actionName) {
        // Implementation for getting block type
        // This would typically involve looking up in a configuration or registry
        if (material != null && actionName != null) {
            // Get all block configs for this material
            java.util.List<BlockConfigService.BlockConfig> configs = blockConfigService.getBlockConfigsForMaterial(material);
            // Find the one that matches the action name
            BlockType blockType = findBlockTypeInConfigs(configs, actionName);
            if (blockType != null) {
                return blockType;
            }
        }
        
        // Fallback to old method if material is not used
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(actionName);
        if (config != null) {
            return getBlockTypeFromString(config.getType());
        }
        return null;
    }
    
    /**
     * Find block type in a list of configurations
     */
    private BlockType findBlockTypeInConfigs(java.util.List<BlockConfigService.BlockConfig> configs, String actionName) {
        for (BlockConfigService.BlockConfig config : configs) {
            if (actionName.equals(config.getId())) {
                return getBlockTypeFromString(config.getType());
            }
        }
        return null;
    }
    
    /**
     * Convert string type to BlockType enum
     */
    private BlockType getBlockTypeFromString(String type) {
        if (BLOCK_TYPE_EVENT.equals(type)) return BlockType.EVENT;
        if (BLOCK_TYPE_ACTION.equals(type)) return BlockType.ACTION;
        if (BLOCK_TYPE_CONDITION.equals(type)) return BlockType.CONDITION;
        if (BLOCK_TYPE_CONTROL.equals(type)) return BlockType.CONTROL;
        if (BLOCK_TYPE_FUNCTION.equals(type)) return BlockType.FUNCTION;
        return null;
    }
    
    // Остальные методы интерфейса (getVariableManager, getDebugger, stopExecution...)
    
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
    
    // 🎆 Reference system-style enhanced execution methods
    
    @Override
    public CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, 
                                                           AdvancedExecutionEngine.ExecutionMode mode, 
                                                           AdvancedExecutionEngine.Priority priority, 
                                                           String trigger) {
        return advancedExecutionEngine.executeScript(script, player, mode, priority, trigger);
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, 
                                                          AdvancedExecutionEngine.ExecutionMode mode, 
                                                          AdvancedExecutionEngine.Priority priority, 
                                                          String trigger) {
        // Create a temporary script with just this block
        CodeScript tempScript = new CodeScript("Temporary Block Script", true, block);
        return advancedExecutionEngine.executeScript(tempScript, player, mode, priority, trigger);
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeScriptDelayed(CodeScript script, Player player, 
                                                                   long delayTicks, String trigger) {
        return advancedExecutionEngine.executeScript(script, player, 
            AdvancedExecutionEngine.ExecutionMode.DELAYED, 
            AdvancedExecutionEngine.Priority.NORMAL, trigger);
    }
    
    @Override
    public CompletableFuture<ExecutionResult[]> executeScriptsBatch(CodeScript[] scripts, Player player, String trigger) {
        CompletableFuture<ExecutionResult>[] futures = new CompletableFuture[scripts.length];
        
        for (int i = 0; i < scripts.length; i++) {
            futures[i] = advancedExecutionEngine.executeScript(scripts[i], player, 
                AdvancedExecutionEngine.ExecutionMode.BATCH, 
                AdvancedExecutionEngine.Priority.NORMAL, trigger);
        }
        
        return CompletableFuture.allOf(futures)
            .thenApply(v -> {
                ExecutionResult[] results = new ExecutionResult[futures.length];
                for (int i = 0; i < futures.length; i++) {
                    try {
                        results[i] = futures[i].get();
                    } catch (Exception e) {
                        results[i] = ExecutionResult.error("Batch execution failed: " + e.getMessage());
                    }
                }
                return results;
            });
    }
    
    @Override
    public void cancelPlayerExecutions(Player player) {
        advancedExecutionEngine.cancelPlayerExecutions(player);
        
        // Also cancel any active executions in the legacy system
        activeExecutions.entrySet().removeIf(entry -> {
            ExecutionContext context = entry.getValue();
            if (context.getPlayer() != null && context.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                context.setCancelled(true);
                return true;
            }
            return false;
        });
    }
    
    @Override
    public AdvancedExecutionEngine.ExecutionStatistics getExecutionStatistics() {
        return advancedExecutionEngine.getStatistics();
    }
    
    @Override
    public void setMaxExecutionTime(long maxTimeMs) {
        this.maxExecutionTimeMs = maxTimeMs;
    }
    
    @Override
    public void setMaxInstructionsPerTick(int maxInstructions) {
        this.maxInstructionsPerTick = maxInstructions;
    }
    
    @Override
    public boolean isOverloaded() {
        AdvancedExecutionEngine.ExecutionStatistics stats = getExecutionStatistics();
        // Consider overloaded if more than 50 active sessions or throughput over 100/sec
        return stats.getActiveSessions() > 50 || stats.getThroughput() > 100.0;
    }
    
    @Override
    public double getCurrentThroughput() {
        return getExecutionStatistics().getThroughput();
    }
    
    /**
     * Validates a script without executing it
     * @param script The script to validate
     * @return ValidationResult containing validation results
     */
    public ScriptValidator.ValidationResult validateScript(CodeScript script) {
        return scriptValidator.validateScript(script);
    }
    
    /**
     * Gets the script validator
     * @return The ScriptValidator instance
     */
    public ScriptValidator getScriptValidator() {
        return scriptValidator;
    }
    
    /**
     * Shutdown the enhanced execution engine
     */
    public void shutdown() {
        if (advancedExecutionEngine != null) {
            advancedExecutionEngine.shutdown();
        }
        
        // Cancel all active executions
        activeExecutions.values().forEach(context -> context.setCancelled(true));
        activeExecutions.clear();
    }
}