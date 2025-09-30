package com.megacreative.coding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.megacreative.MegaCreative;
import com.megacreative.coding.cache.BlockExecutionCache;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.coding.events.EventPublisher;
import com.megacreative.coding.executors.AdvancedExecutionEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.interfaces.IScriptEngine;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.events.CustomEventManager;

public class DefaultScriptEngine implements ScriptEngine, EnhancedScriptEngine, EventPublisher, IScriptEngine {
    
    // Constants for block types
    private static final String BLOCK_TYPE_EVENT = "EVENT";
    private static final String BLOCK_TYPE_ACTION = "ACTION";
    private static final String BLOCK_TYPE_CONDITION = "CONDITION";
    private static final String BLOCK_TYPE_CONTROL = "CONTROL";
    private static final String BLOCK_TYPE_FUNCTION = "FUNCTION";
    
    // Constants for execution events
    private static final String EVENT_SCRIPT_START = "script_execution_start";
    private static final String EVENT_SCRIPT_END = "script_execution_end";
    private static final String EVENT_BLOCK_EXECUTE = "block_execute";
    private static final String EVENT_BLOCK_SUCCESS = "block_execute_success";
    private static final String EVENT_BLOCK_ERROR = "block_execute_error";
    
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
    
    // Strategy pattern: Map of block executors
    private final Map<BlockType, BlockExecutor> executors = new HashMap<>();
    
    // Event manager for publishing events
    private CustomEventManager eventManager;
    
    public DefaultScriptEngine(MegaCreative plugin, VariableManager variableManager, VisualDebugger debugger,
                               BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        this.debugger = debugger;
        this.blockConfigService = blockConfigService;
        
        // 🎆 Reference system: Initialize advanced execution engine
        this.advancedExecutionEngine = new AdvancedExecutionEngine(plugin);
        
        // Initialize script validator
        this.scriptValidator = new ScriptValidator(blockConfigService);
        
        // Initialize execution cache with 5 minute TTL and max 1000 entries
        this.executionCache = new BlockExecutionCache(5L, TimeUnit.MINUTES, 1000);
        
        // Schedule cache cleanup every minute
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, executionCache::cleanup, 600, 600);
        
        // Initialize block executors (Strategy pattern)
        initializeExecutors();
    }
    
    /**
     * Initialize block executors for the Strategy pattern
     */
    private void initializeExecutors() {
        // Create factories (these would typically be injected)
        ActionFactory actionFactory = new ActionFactory(plugin);
        ConditionFactory conditionFactory = new ConditionFactory(plugin);
        
        // Register executors for each block type
        executors.put(BlockType.EVENT, new EventBlockExecutor());
        executors.put(BlockType.ACTION, new ActionBlockExecutor(actionFactory));
        executors.put(BlockType.CONDITION, new ConditionBlockExecutor(conditionFactory));
        executors.put(BlockType.CONTROL, new ControlFlowBlockExecutor(actionFactory, conditionFactory));
        executors.put(BlockType.FUNCTION, new FunctionBlockExecutor());
        
        // Initialize the action and condition factories
        actionFactory.registerAllActions();
        conditionFactory.registerAllConditions();
    }
    
    public void initialize() {
        // Initialize the script engine with any required setup
        plugin.getLogger().info("Initializing ScriptEngine with Strategy pattern executors");
    }
    
    public int getActionCount() {
        // Get action count from the action factory
        ActionFactory actionFactory = new ActionFactory(plugin);
        return actionFactory.getActionCount();
    }
    
    public int getConditionCount() {
        // Get condition count from the condition factory
        ConditionFactory conditionFactory = new ConditionFactory(plugin);
        return conditionFactory.getConditionCount();
    }
    
    /**
     * Publishes an event to the event system.
     * 
     * @param event The event to publish
     */
    @Override
    public void publishEvent(CustomEvent event) {
        // Get the event manager from the service registry
        if (eventManager == null) {
            eventManager = plugin.getServiceRegistry().getService(CustomEventManager.class);
        }
        
        // If we have an event manager, use it to trigger the event
        if (eventManager != null) {
            try {
                // Create event data map
                Map<String, DataValue> eventData = new HashMap<>();
                
                // Add basic event information
                eventData.put("event_id", DataValue.fromObject(event.getId().toString()));
                eventData.put("event_name", DataValue.fromObject(event.getName()));
                eventData.put("event_category", DataValue.fromObject(event.getCategory()));
                eventData.put("event_description", DataValue.fromObject(event.getDescription()));
                eventData.put("event_author", DataValue.fromObject(event.getAuthor()));
                eventData.put("event_created_time", DataValue.fromObject(event.getCreatedTime()));
                
                // Add event data fields
                for (Map.Entry<String, CustomEvent.EventDataField> entry : event.getDataFields().entrySet()) {
                    eventData.put("data_" + entry.getKey(), DataValue.fromObject(entry.getKey()));
                }
                
                // Trigger the event through the event manager
                eventManager.triggerEvent(event.getName(), eventData, null, "global");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to publish event through CustomEventManager: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Fallback to logging if no event manager is available
            plugin.getLogger().info("Published event: " + event.getName());
        }
    }
    
    /**
     * Publishes an event with associated data to the event system.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     */
    @Override
    public void publishEvent(String eventName, Map<String, DataValue> eventData) {
        // Get the event manager from the service registry
        if (eventManager == null) {
            eventManager = plugin.getServiceRegistry().getService(CustomEventManager.class);
        }
        
        // If we have an event manager, use it to trigger the event
        if (eventManager != null) {
            try {
                // Trigger the event through the event manager
                eventManager.triggerEvent(eventName, eventData, null, "global");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to publish event through CustomEventManager: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Fallback to logging if no event manager is available
            plugin.getLogger().info("Published event: " + eventName + " with data: " + eventData.size() + " fields");
        }
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
            .creativeWorld(plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld()))
            .currentBlock(script.getRootBlock())
            .build();
        
        activeExecutions.put(executionId, context);
        
        // Publish script start event
        Map<String, DataValue> startEventData = new HashMap<>();
        startEventData.put("script_id", DataValue.fromObject(script.getId()));
        startEventData.put("player_name", DataValue.fromObject(getPlayerName(player)));
        startEventData.put("trigger", DataValue.fromObject(trigger));
        publishEvent(EVENT_SCRIPT_START, startEventData);

        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
        
        // Use Bukkit scheduler to run on main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    plugin.getLogger().info("Starting script execution for player: " + getPlayerName(player));
                    if (debugger.isDebugging(player)) {
                        debugger.onScriptStart(player, script);
                    }
                    ExecutionResult result = processBlock(script.getRootBlock(), context, 0);
                    
                    // Handle special result types with switch-case as requested
                    switch (getResultType(result)) {
                        case PAUSE:
                            // Schedule continuation after pause
                            long ticks = result.getPauseTicks();
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                try {
                                    ExecutionResult nextResult = processBlock(script.getRootBlock().getNextBlock(), context, 0);
                                    completeScriptExecution(nextResult, future, script, player, trigger, executionId);
                                } catch (Exception e) {
                                    future.completeExceptionally(e);
                                    activeExecutions.remove(executionId);
                                }
                            }, ticks);
                            return; // Don't complete the future yet
                        case AWAIT:
                            // Attach continuation to the future
                            result.getAwaitFuture().thenRun(() -> {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    try {
                                        ExecutionResult nextResult = processBlock(script.getRootBlock().getNextBlock(), context, 0);
                                        completeScriptExecution(nextResult, future, script, player, trigger, executionId);
                                    } catch (Exception e) {
                                        future.completeExceptionally(e);
                                        activeExecutions.remove(executionId);
                                    }
                                });
                            }).exceptionally(throwable -> {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    future.completeExceptionally(throwable);
                                    activeExecutions.remove(executionId);
                                });
                                return null;
                            });
                            return; // Don't complete the future yet
                        case NORMAL:
                        default:
                            // Continue with normal completion
                            completeScriptExecution(result, future, script, player, trigger, executionId);
                            break;
                    }
                } catch (Exception e) {
                    String errorMsg = "Script execution error: " + e.getMessage();
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, e);
                    future.completeExceptionally(e);
                    
                    // Publish script error event
                    Map<String, DataValue> errorEventData = new HashMap<>();
                    errorEventData.put("script_id", DataValue.fromObject(script.getId()));
                    errorEventData.put("player_name", DataValue.fromObject(getPlayerName(player)));
                    errorEventData.put("trigger", DataValue.fromObject(trigger));
                    errorEventData.put("error", DataValue.fromObject(e.getMessage()));
                    publishEvent(EVENT_BLOCK_ERROR, errorEventData);
                    
                    activeExecutions.remove(executionId);
                } catch (Throwable t) {
                    String errorMsg = "Critical script execution error: " + t.getMessage();
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, t);
                    future.completeExceptionally(new RuntimeException(Constants.CRITICAL_EXECUTION_ERROR, t));
                    
                    // Publish critical error event
                    Map<String, DataValue> errorEventData = new HashMap<>();
                    errorEventData.put("script_id", DataValue.fromObject(script.getId()));
                    errorEventData.put("player_name", DataValue.fromObject(getPlayerName(player)));
                    errorEventData.put("trigger", DataValue.fromObject(trigger));
                    errorEventData.put("error", DataValue.fromObject(t.getMessage()));
                    errorEventData.put("critical", DataValue.fromObject(true));
                    publishEvent(EVENT_BLOCK_ERROR, errorEventData);
                    
                    activeExecutions.remove(executionId);
                }
            }
        }.runTask(plugin);

        return future;
    }
    
    /**
     * Completes script execution and handles cleanup
     */
    private void completeScriptExecution(ExecutionResult result, CompletableFuture<ExecutionResult> future, 
                                       CodeScript script, Player player, String trigger, String executionId) {
        try {
            future.complete(result);
            
            // Publish script end event
            Map<String, DataValue> endEventData = new HashMap<>();
            endEventData.put("script_id", DataValue.fromObject(script.getId()));
            endEventData.put("player_name", DataValue.fromObject(getPlayerName(player)));
            endEventData.put("trigger", DataValue.fromObject(trigger));
            endEventData.put("success", DataValue.fromObject(result.isSuccess()));
            endEventData.put("message", DataValue.fromObject(result.getMessage()));
            publishEvent(EVENT_SCRIPT_END, endEventData);
        } finally {
            if (debugger.isDebugging(player)) {
                debugger.onScriptEnd(player, script);
            }
            activeExecutions.remove(executionId);
        }
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, String trigger) {
        if (block == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success(Constants.BLOCK_IS_NULL));
        }

        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .creativeWorld(plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld()))
            .currentBlock(block)
            .build();

        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
        
        // Use Bukkit scheduler to run on main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    plugin.getLogger().info("Starting block execution for player: " + getPlayerName(player) + 
                                          " with action: " + (block != null ? block.getAction() : "null"));
                    ExecutionResult result = processBlock(block, context, 0);
                    
                    // Handle special result types with switch-case as requested
                    switch (getResultType(result)) {
                        case PAUSE:
                            // Schedule continuation after pause
                            long ticks = result.getPauseTicks();
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                try {
                                    ExecutionResult nextResult = processBlock(block.getNextBlock(), context, 0);
                                    future.complete(nextResult);
                                } catch (Exception e) {
                                    future.completeExceptionally(e);
                                }
                            }, ticks);
                            return; // Don't complete the future yet
                        case AWAIT:
                            // Attach continuation to the future
                            result.getAwaitFuture().thenRun(() -> {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    try {
                                        ExecutionResult nextResult = processBlock(block.getNextBlock(), context, 0);
                                        future.complete(nextResult);
                                    } catch (Exception e) {
                                        future.completeExceptionally(e);
                                    }
                                });
                            }).exceptionally(throwable -> {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    future.completeExceptionally(throwable);
                                });
                                return null;
                            });
                            return; // Don't complete the future yet
                        case NORMAL:
                        default:
                            // Continue with normal completion
                            future.complete(result);
                            break;
                    }
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
            .creativeWorld(plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld()))
            .currentBlock(startBlock)
            .build();

        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
        
        // Use Bukkit scheduler to run on main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    plugin.getLogger().info("Starting block chain execution for player: " + getPlayerName(player) + 
                                          " with start block action: " + (startBlock != null ? startBlock.getAction() : "null"));
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
     * Safely gets player name, handling null cases
     */
    private String getPlayerName(Player player) {
        return player != null ? player.getName() : "Unknown";
    }
    
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
        // Log block processing start
        plugin.getLogger().info("Processing block at recursion depth " + recursionDepth + 
                              " with action: " + (block != null ? block.getAction() : "null"));
        
        // Check cache first
        Map<String, Object> cacheContext = createCacheContext(context);
        ExecutionResult cachedResult = executionCache.get(block, cacheContext);
        if (cachedResult != null) {
            plugin.getLogger().info("Returning cached result for block with action: " + (block != null ? block.getAction() : "null"));
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

        // Determine the block type properly instead of hardcoding to "ACTION"
        BlockType blockType = BlockType.ACTION; // Default to action
        if (block != null && block.getAction() != null) {
            BlockType type = getBlockType(block.getMaterial(), block.getAction());
            if (type != null) {
                blockType = type;
            }
        }

        ExecutionResult result;

        try {
            // --- ОСНОВНАЯ ЛОГИКА ---
            // --- MAIN LOGIC ---
            // --- HAUPTLOGIK ---
            
            plugin.getLogger().info("Processing block of type: " + blockType + " with action: " + (block != null ? block.getAction() : "null"));
            
            // Use Strategy pattern to execute the block
            BlockExecutor executor = executors.get(blockType);
            if (executor != null) {
                result = executor.execute(block, context);
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
            
            // Handle special result types with switch-case as requested
            switch (getResultType(result)) {
                case PAUSE:
                    // Return the pause result - the caller will handle scheduling
                    plugin.getLogger().info("Block execution paused for " + result.getPauseTicks() + " ticks");
                    return result;
                case AWAIT:
                    // Return the await result - the caller will handle the future
                    plugin.getLogger().info("Block execution awaiting CompletableFuture");
                    return result;
                case NORMAL:
                default:
                    // Continue with normal processing
                    break;
            }
            
            // Handle conditional branches specifically
            if (blockType == BlockType.CONTROL && "conditionalBranch".equals(block.getAction())) {
                // Get the condition result from the execution result details
                Object conditionResultObj = result.getDetail("condition_result");
                if (conditionResultObj instanceof Boolean) {
                    boolean conditionResult = (Boolean) conditionResultObj;
                    
                    if (conditionResult) {
                        // Condition is true, execute the true branch (children)
                        plugin.getLogger().info("Condition is true, executing true branch");
                        if (!block.getChildren().isEmpty()) {
                            // Execute the first child block in the true branch
                            CodeBlock trueBranch = block.getChildren().get(0);
                            return processBlock(trueBranch, context, recursionDepth + 1);
                        }
                    } else {
                        // Condition is false, look for an else block
                        plugin.getLogger().info("Condition is false, looking for else branch");
                        CodeBlock elseBlock = findElseBlock(block);
                        if (elseBlock != null) {
                            plugin.getLogger().info("Found else block, executing else branch");
                            return processBlock(elseBlock, context, recursionDepth + 1);
                        }
                    }
                }
            }
        
            // Cache the result before returning
            if (result != null && result.isSuccess() && isCacheable(block)) {
                executionCache.put(block, cacheContext, result);
            }
        
            plugin.getLogger().info("Continuing to next block in chain");
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
     * Determine the type of ExecutionResult for switch-case handling
     */
    private ResultType getResultType(ExecutionResult result) {
        if (result == null) {
            return ResultType.NORMAL;
        }
        
        if (result.isPause()) {
            return ResultType.PAUSE;
        }
        
        if (result.isAwait()) {
            return ResultType.AWAIT;
        }
        
        return ResultType.NORMAL;
    }
    
    /**
     * Enum for ExecutionResult types to enable switch-case handling
     */
    private enum ResultType {
        NORMAL,
        PAUSE,
        AWAIT
    }
    
    /**
     * Finds the else block associated with a conditional branch
     * 
     * @param conditionalBlock The conditional branch block
     * @return The else block if found, null otherwise
     */
    private CodeBlock findElseBlock(CodeBlock conditionalBlock) {
        // Look for the else block in the chain after the conditional block's children
        if (conditionalBlock == null) {
            return null;
        }
        
        // First, find the end of the true branch
        CodeBlock current = null;
        
        // If there are children, the true branch is the chain of children
        if (!conditionalBlock.getChildren().isEmpty()) {
            // Get the last child in the true branch
            current = conditionalBlock.getChildren().get(conditionalBlock.getChildren().size() - 1);
            
            // Follow the chain to the end of the true branch
            while (current != null && current.getNextBlock() != null) {
                current = current.getNextBlock();
            }
        } else {
            // If no children, the true branch is just the next block
            current = conditionalBlock.getNextBlock();
            
            // Follow the chain to the end of the true branch
            while (current != null && current.getNextBlock() != null && !"else".equals(current.getAction())) {
                current = current.getNextBlock();
            }
        }
        
        // Check if the current block is an else block
        if (current != null && "else".equals(current.getAction())) {
            return current;
        }
        
        // If we didn't find an else block directly, look in the next block
        if (conditionalBlock.getNextBlock() != null) {
            current = conditionalBlock.getNextBlock();
            while (current != null) {
                if ("else".equals(current.getAction())) {
                    return current;
                }
                current = current.getNextBlock();
            }
        }
        
        return null;
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
        // Log block chain processing start
        plugin.getLogger().info("Processing block chain at recursion depth " + recursionDepth + 
                              " with block action: " + (block != null ? block.getAction() : "null"));
        
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
            
            // Handle special result types with switch-case as requested
            switch (getResultType(result)) {
                case PAUSE:
                    // Return the pause result - the caller will handle scheduling
                    plugin.getLogger().info("Block execution paused for " + result.getPauseTicks() + " ticks in chain");
                    return result;
                case AWAIT:
                    // Return the await result - the caller will handle the future
                    plugin.getLogger().info("Block execution awaiting CompletableFuture in chain");
                    return result;
                case NORMAL:
                default:
                    // Continue with normal processing
                    break;
            }
            
            // If the result indicates termination (break/continue), return it
            if (result.isTerminated()) {
                plugin.getLogger().info("Block execution terminated");
                return result;
            }
            
            // If there's an error, return it
            if (result != null && !result.isSuccess()) {
                plugin.getLogger().warning("Block execution failed: " + result.getMessage());
                return result;
            }
            
            // Continue to the next block in the chain
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock != null) {
                plugin.getLogger().info("Continuing to next block in chain with action: " + nextBlock.getAction());
                return processBlockChain(nextBlock, context, executionChain, recursionDepth + 1);
            }
            
            // End of chain
            plugin.getLogger().info("End of block chain reached");
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
        // With the new annotation-based system, manual registration is not needed
        plugin.getLogger().info("Action registration via ScriptEngine is deprecated. Use @BlockMeta annotation instead.");
    }

    @Override
    public void registerCondition(BlockType type, BlockCondition condition) {
        // Implementation for registering conditions
        // With the new annotation-based system, manual registration is not needed
        plugin.getLogger().info("Condition registration via ScriptEngine is deprecated. Use @BlockMeta annotation instead.");
    }
    
    /**
     * Register an action with a specific ID
     * 
     * @param actionId The action ID
     * @param action The BlockAction implementation
     */
    public void registerAction(String actionId, BlockAction action) {
        // With the new annotation-based system, manual registration is not needed
        plugin.getLogger().info("Action registration via ScriptEngine is deprecated. Use @BlockMeta annotation instead.");
    }
    
    /**
     * Register a condition with a specific ID
     * 
     * @param conditionId The condition ID
     * @param condition The BlockCondition implementation
     */
    public void registerCondition(String conditionId, BlockCondition condition) {
        // With the new annotation-based system, manual registration is not needed
        plugin.getLogger().info("Condition registration via ScriptEngine is deprecated. Use @BlockMeta annotation instead.");
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
        // Implementation for enhanced script execution
        return advancedExecutionEngine.executeScript(script, player, mode, priority, trigger);
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, 
                                                          AdvancedExecutionEngine.ExecutionMode mode, 
                                                          AdvancedExecutionEngine.Priority priority, 
                                                          String trigger) {
        // Implementation for enhanced block execution
        // For now, we'll execute the block as a single-script execution
        if (block == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success(Constants.BLOCK_IS_NULL));
        }
        
        // Create a temporary script containing just this block
        CodeScript tempScript = new CodeScript(block);
        // The script is enabled by default in the constructor
        
        return advancedExecutionEngine.executeScript(tempScript, player, mode, priority, trigger);
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeScriptDelayed(CodeScript script, Player player, 
                                                                 long delayTicks, String trigger) {
        // Implementation for delayed script execution
        return advancedExecutionEngine.executeScript(script, player, 
            AdvancedExecutionEngine.ExecutionMode.DELAYED, 
            AdvancedExecutionEngine.Priority.NORMAL, 
            trigger);
    }
    

    
    // Getters for performance settings
    public long getMaxExecutionTimeMs() {
        return maxExecutionTimeMs;
    }
    
    public void setMaxExecutionTimeMs(long maxExecutionTimeMs) {
        this.maxExecutionTimeMs = maxExecutionTimeMs;
    }
    
    public int getMaxInstructionsPerTick() {
        return maxInstructionsPerTick;
    }
    
    /**
     * Set the maximum instructions per tick to prevent lag
     * 
     * @param maxInstructions Maximum instructions per server tick
     */
    @Override
    public void setMaxInstructionsPerTick(int maxInstructions) {
        this.maxInstructionsPerTick = maxInstructions;
    }
    
    /**
     * Set the maximum execution time for scripts
     * 
     * @param maxTimeMs Maximum execution time in milliseconds
     */
    @Override
    public void setMaxExecutionTime(long maxTimeMs) {
        this.maxExecutionTimeMs = maxTimeMs;
    }
    

    
    /**
     * Cancel all executions for a specific player
     * 
     * @param player The player whose executions to cancel
     */
    @Override
    public void cancelPlayerExecutions(Player player) {
        // Delegate to the advanced execution engine
        advancedExecutionEngine.cancelPlayerExecutions(player);
    }
    
    /**
     * Get execution statistics and performance metrics
     * 
     * @return Execution statistics
     */
    @Override
    public AdvancedExecutionEngine.ExecutionStatistics getExecutionStatistics() {
        return advancedExecutionEngine.getStatistics();
    }
    
    /**
     * Check if the engine is currently overloaded
     * 
     * @return true if the engine is under heavy load
     */
    @Override
    public boolean isOverloaded() {
        // Check if we're overloaded based on active sessions and thread pool
        return activeExecutions.size() > 50 || // More than 50 active executions
               advancedExecutionEngine.getStatistics().getActiveThreads() >= 4; // All threads busy
    }
    
    /**
     * Get the current throughput (executions per second)
     * 
     * @return Current execution throughput
     */
    @Override
    public double getCurrentThroughput() {
        // Get throughput from the advanced execution engine statistics
        AdvancedExecutionEngine.ExecutionStatistics stats = advancedExecutionEngine.getStatistics();
        return stats != null ? stats.getThroughput() : 0.0;
    }
    
    /**
     * Execute multiple scripts in batch mode
     * 
     * @param scripts Array of scripts to execute
     * @param player The player context
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with batch execution result
     */
    @Override
    public CompletableFuture<ExecutionResult[]> executeScriptsBatch(CodeScript[] scripts, Player player, String trigger) {
        // Implementation for batch script execution
        if (scripts == null || scripts.length == 0) {
            return CompletableFuture.completedFuture(new ExecutionResult[0]);
        }
        
        CompletableFuture<ExecutionResult>[] futures = new CompletableFuture[scripts.length];
        for (int i = 0; i < scripts.length; i++) {
            futures[i] = executeScript(scripts[i], player, trigger);
        }
        
        return CompletableFuture.allOf(futures)
            .thenApply(v -> java.util.Arrays.stream(futures)
                .map(CompletableFuture::join)
                .toArray(ExecutionResult[]::new));
    }
}