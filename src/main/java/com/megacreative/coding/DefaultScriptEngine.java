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
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.variables.VariableManager;
 
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.events.CustomEventManager;

public class DefaultScriptEngine implements ScriptEngine, EnhancedScriptEngine, EventPublisher {
    
    
    private static final String BLOCK_TYPE_EVENT = "EVENT";
    private static final String BLOCK_TYPE_ACTION = "ACTION";
    private static final String BLOCK_TYPE_CONDITION = "CONDITION";
    private static final String BLOCK_TYPE_CONTROL = "CONTROL";
    private static final String BLOCK_TYPE_FUNCTION = "FUNCTION";
    
    
    private static final String EVENT_SCRIPT_START = "script_execution_start";
    private static final String EVENT_SCRIPT_END = "script_execution_end";
    private static final String EVENT_BLOCK_EXECUTE = "block_execute";
    private static final String EVENT_BLOCK_SUCCESS = "block_execute_success";
    private static final String EVENT_BLOCK_ERROR = "block_execute_error";
    
    
    private static final String CONTROL_ACTION_CONDITIONAL_BRANCH = "conditionalBranch";
    private static final String CONTROL_ACTION_ELSE = "else";
    private static final String CONTROL_ACTION_WHILE_LOOP = "whileLoop";
    private static final String CONTROL_ACTION_FOR_EACH = "forEach";
    private static final String CONTROL_ACTION_BREAK = "break";
    private static final String CONTROL_ACTION_CONTINUE = "continue";
    
    
    private static final String FUNCTION_ACTION_CALL_FUNCTION = "callFunction";
    
    
    private static final String BRACKET_OPEN = "OPEN";
    private static final String BRACKET_CLOSE = "CLOSE";
    
    
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
    
    
    private final AdvancedExecutionEngine advancedExecutionEngine;
    
    
    private final ScriptValidator scriptValidator;
    
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    private static final int MAX_RECURSION_DEPTH = 100;
    private final BlockExecutionCache executionCache;
    
    
    private long maxExecutionTimeMs = 5000; 
    private int maxInstructionsPerTick = 1000;
    
    
    private final Map<BlockType, BlockExecutor> executors = new HashMap<>();
    
    
    private CustomEventManager eventManager;
    
    
    private ConditionFactory conditionFactory;
    
    public DefaultScriptEngine(MegaCreative plugin, VariableManager variableManager, VisualDebugger debugger,
                               BlockConfigService blockConfigService, ScriptValidator scriptValidator) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        this.debugger = debugger;
        this.blockConfigService = blockConfigService;
        
        
        this.advancedExecutionEngine = new AdvancedExecutionEngine(plugin);
        
        
        this.scriptValidator = scriptValidator != null ? scriptValidator : new ScriptValidator(blockConfigService);
        
        
        this.executionCache = new BlockExecutionCache(5L, TimeUnit.MINUTES, 1000);
        
        
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, executionCache::cleanup, 600, 600);
        
        
        initializeExecutors();
    }
    
    /**
     * Initialize block executors for the Strategy pattern
     */
    private void initializeExecutors() {
        ActionFactory actionFactory = plugin.getServiceRegistry().getActionFactory() instanceof ActionFactory
            ? (ActionFactory) plugin.getServiceRegistry().getActionFactory()
            : new ActionFactory(plugin);
        this.conditionFactory = plugin.getServiceRegistry().getConditionFactory() instanceof ConditionFactory
            ? (ConditionFactory) plugin.getServiceRegistry().getConditionFactory()
            : new ConditionFactory(plugin);

        executors.put(BlockType.EVENT, new EventBlockExecutor());
        executors.put(BlockType.ACTION, new ActionBlockExecutor(actionFactory));
        executors.put(BlockType.CONDITION, new ConditionBlockExecutor(conditionFactory));
        executors.put(BlockType.CONTROL, new ControlFlowBlockExecutor(actionFactory, conditionFactory));
        executors.put(BlockType.FUNCTION, new FunctionBlockExecutor());
    }
    
    public void initialize() {
        
        plugin.getLogger().info("Initializing ScriptEngine with Strategy pattern executors");
    }
    
    public int getActionCount() {
        ActionFactory actionFactory = plugin.getServiceRegistry().getActionFactory() instanceof ActionFactory
            ? (ActionFactory) plugin.getServiceRegistry().getActionFactory()
            : new ActionFactory(plugin);
        return actionFactory.getActionCount();
    }
    
    public int getConditionCount() {
        ConditionFactory conditionFactory = plugin.getServiceRegistry().getConditionFactory() instanceof ConditionFactory
            ? (ConditionFactory) plugin.getServiceRegistry().getConditionFactory()
            : new ConditionFactory(plugin);
        return conditionFactory.getConditionCount();
    }
    
    /**
     * Publishes an event to the event system.
     * 
     * @param event The event to publish
     */
    @Override
    public void publishEvent(CustomEvent event) {
        
        if (eventManager == null) {
            eventManager = plugin.getServiceRegistry().getService(CustomEventManager.class);
        }
        
        
        if (eventManager != null) {
            try {
                
                Map<String, DataValue> eventData = new HashMap<>();
                
                
                eventData.put("event_id", DataValue.fromObject(event.getId().toString()));
                eventData.put("event_name", DataValue.fromObject(event.getName()));
                eventData.put("event_category", DataValue.fromObject(event.getCategory()));
                eventData.put("event_description", DataValue.fromObject(event.getDescription()));
                eventData.put("event_author", DataValue.fromObject(event.getAuthor()));
                eventData.put("event_created_time", DataValue.fromObject(event.getCreatedTime()));
                
                
                for (Map.Entry<String, CustomEvent.EventDataField> entry : event.getDataFields().entrySet()) {
                    eventData.put("data_" + entry.getKey(), DataValue.fromObject(entry.getKey()));
                }
                
                
                eventManager.triggerEvent(event.getName(), eventData, null, "global");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to publish event through CustomEventManager: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            
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
        
        if (eventManager == null) {
            eventManager = plugin.getServiceRegistry().getService(CustomEventManager.class);
        }
        
        
        if (eventManager != null) {
            try {
                
                eventManager.triggerEvent(eventName, eventData, null, "global");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to publish event through CustomEventManager: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            
            plugin.getLogger().info("Published event: " + eventName + " with data: " + eventData.size() + " fields");
        }
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, String trigger) {
        if (script == null || !script.isEnabled() || script.getRootBlock() == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success(Constants.SCRIPT_IS_INVALID_OR_DISABLED));
        }

        
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
        
        
        Map<String, DataValue> startEventData = new HashMap<>();
        startEventData.put("script_id", DataValue.fromObject(script.getId()));
        startEventData.put("player_name", DataValue.fromObject(getPlayerName(player)));
        startEventData.put("trigger", DataValue.fromObject(trigger));
        publishEvent(EVENT_SCRIPT_START, startEventData);

        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
        
        
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    plugin.getLogger().info("Starting script execution for player: " + getPlayerName(player));
                    if (debugger.isDebugging(player)) {
                        debugger.onScriptStart(player, script);
                    }
                    ExecutionResult result = processBlock(script.getRootBlock(), context, 0);
                    
                    
                    switch (getResultType(result)) {
                        case PAUSE:
                            
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
                            return; 
                        case AWAIT:
                            
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
                            return; 
                        case NORMAL:
                        default:
                            
                            completeScriptExecution(result, future, script, player, trigger, executionId);
                            break;
                    }
                } catch (Exception e) {
                    String errorMsg = "Script execution error: " + e.getMessage();
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, e);
                    future.completeExceptionally(e);
                    
                    
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
        
        
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    plugin.getLogger().info("Starting block execution for player: " + getPlayerName(player) + 
                                          " with action: " + block.getAction());
                    ExecutionResult result = processBlock(block, context, 0);
                    
                    
                    switch (getResultType(result)) {
                        case PAUSE:
                            
                            long ticks = result.getPauseTicks();
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                try {
                                    ExecutionResult nextResult = processBlock(block.getNextBlock(), context, 0);
                                    future.complete(nextResult);
                                } catch (Exception e) {
                                    future.completeExceptionally(e);
                                }
                            }, ticks);
                            return; 
                        case AWAIT:
                            
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
                            return; 
                        case NORMAL:
                        default:
                            
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

        
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .creativeWorld(plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld()))
            .currentBlock(startBlock)
            .build();

        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
        
        
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    plugin.getLogger().info("Starting block chain execution for player: " + getPlayerName(player) + 
                                          " with start block action: " + startBlock.getAction());
                    
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
        
        return cacheContext;
    }
    
    /**
     * Checks if a block's execution result can be cached
     */
    private boolean isCacheable(CodeBlock block) {
        if (block == null) return false;
        
        
        String action = block.getAction();
        if (action == null) return false;
        
        
        String lowerAction = action.toLowerCase();
        return !lowerAction.contains("random") && 
               !lowerAction.contains("spawn") &&
               !lowerAction.contains("give") &&
               !lowerAction.contains("teleport") &&
               !lowerAction.contains("wait") &&
               !lowerAction.contains("pause");
    }
    
    private ExecutionResult processBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
        
        plugin.getLogger().info("Processing block at recursion depth " + recursionDepth + 
                              " with action: " + (block != null ? block.getAction() : "null"));
        
        
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
                
                ExecutionResult errorResult = ExecutionResult.error(errorMsg);
                if (isCacheable(block)) {
                    executionCache.put(block, cacheContext, errorResult);
                }
                return errorResult;
            }
            
            ExecutionResult successResult = ExecutionResult.success(Constants.BLOCK_IS_NULL);
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, successResult);
            }
            return successResult;
        }

        
        if (context.isCancelled()) {
            ExecutionResult cancelledResult = ExecutionResult.success("Execution cancelled");
            
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, cancelledResult);
            }
            return cancelledResult;
        }

        
        if (context.isPaused()) {
            ExecutionResult pausedResult = ExecutionResult.success("Execution paused").withPause();
            
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, pausedResult);
            }
            return pausedResult;
        }

        
        if (context.isStepping()) {
            context.setStepping(false);
            context.setPaused(true);
            ExecutionResult stepResult = ExecutionResult.success("Execution step").withPause();
            
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, stepResult);
            }
            return stepResult;
        }

        
        long startTime = System.currentTimeMillis();
        if (startTime - context.getStartTime() > maxExecutionTimeMs) {
            String errorMsg = "Script execution timed out after " + maxExecutionTimeMs + "ms";
            plugin.getLogger().warning(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            
            ExecutionResult timeoutResult = ExecutionResult.error(errorMsg);
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, timeoutResult);
            }
            return timeoutResult;
        }

        
        BlockType blockType = BlockType.ACTION; 
        // Condition block != null is always true
        // Removed redundant null check since we already check for null above
        if (block.getAction() != null) {
            BlockType type = getBlockType(block.getMaterial(), block.getAction());
            if (type != null) {
                blockType = type;
            }
        }

        ExecutionResult result;

        try {
            
            
            
            
            plugin.getLogger().info("Processing block of type: " + blockType + " with action: " + (block != null ? block.getAction() : "null"));
            
            
            BlockExecutor executor = executors.get(blockType);
            if (executor != null) {
                result = executor.execute(block, context);
            } else {
                String errorMsg = ERROR_UNSUPPORTED_BLOCK_TYPE + blockType;
                plugin.getLogger().warning(errorMsg + " Player: " + 
                    (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                
                ExecutionResult errorResult = ExecutionResult.error(errorMsg);
                if (isCacheable(block)) {
                    executionCache.put(block, cacheContext, errorResult);
                }
                return processBlock(block.getNextBlock(), context, recursionDepth + 1);
            }
            
            
            switch (getResultType(result)) {
                case PAUSE:
                    
                    plugin.getLogger().info("Block execution paused for " + result.getPauseTicks() + " ticks");
                    return result;
                case AWAIT:
                    
                    plugin.getLogger().info("Block execution awaiting CompletableFuture");
                    return result;
                case NORMAL:
                default:
                    
                    break;
            }
            
            
            if (blockType == BlockType.CONTROL && "conditionalBranch".equals(block.getAction())) {
                
                Object conditionResultObj = result.getDetail("condition_result");
                if (conditionResultObj instanceof Boolean) {
                    boolean conditionResult = (Boolean) conditionResultObj;
                    
                    if (conditionResult) {
                        
                        plugin.getLogger().info("Condition is true, executing true branch");
                        if (!block.getChildren().isEmpty()) {
                            
                            CodeBlock trueBranch = block.getChildren().get(0);
                            return processBlock(trueBranch, context, recursionDepth + 1);
                        }
                    } else {
                        
                        plugin.getLogger().info("Condition is false, looking for else branch");
                        CodeBlock elseBlock = findElseBlock(block);
                        if (elseBlock != null) {
                            plugin.getLogger().info("Found else block, executing else branch");
                            return processBlock(elseBlock, context, recursionDepth + 1);
                        }
                    }
                }
            }
            
            
            if (blockType == BlockType.CONTROL && "whileLoop".equals(block.getAction())) {
                
                Object conditionIdObj = result.getDetail("condition_id");
                Object maxIterationsObj = result.getDetail("max_iterations");
                
                if (conditionIdObj instanceof String && maxIterationsObj instanceof Integer) {
                    String conditionId = (String) conditionIdObj;
                    int maxIterations = (Integer) maxIterationsObj;
                    
                    plugin.getLogger().info("Initializing while loop with condition: " + conditionId + ", max iterations: " + maxIterations);
                    
                    
                    context.setVariable("_while_condition", conditionId);
                    context.setVariable("_while_max_iterations", maxIterations);
                    context.setVariable("_while_iteration_count", 0);
                    
                    
                    return executeWhileLoop(block, context, recursionDepth);
                }
            }
            
            
            if (blockType == BlockType.CONTROL && "forEach".equals(block.getAction())) {
                
                Object collectionNameObj = result.getDetail("collection_name");
                Object variableNameObj = result.getDetail("variable_name");
                
                if (collectionNameObj instanceof String && variableNameObj instanceof String) {
                    String collectionName = (String) collectionNameObj;
                    String variableName = (String) variableNameObj;
                    
                    plugin.getLogger().info("Initializing for-each loop over collection: " + collectionName + " with variable: " + variableName);
                    
                    
                    context.setVariable("_foreach_collection", collectionName);
                    context.setVariable("_foreach_variable", variableName);
                    context.setVariable("_foreach_index", 0);
                    
                    
                    return executeForEachLoop(block, context, recursionDepth);
                }
            }
        
            
            if (result.isSuccess() && isCacheable(block)) {
                executionCache.put(block, cacheContext, result);
            }
        
            plugin.getLogger().info("Continuing to next block in chain");
            return processBlock(block.getNextBlock(), context, recursionDepth + 1);
        } catch (Exception e) {
            String errorMsg = ERROR_EXECUTING_BLOCK + block.getAction() + ": " + e.getMessage() + 
                " Player: " + (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown");
            plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, e);
            
            ExecutionResult errorResult = ExecutionResult.error(errorMsg);
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, errorResult);
            }
            return ExecutionResult.error(errorMsg);
        } catch (Throwable t) {
            String errorMsg = ERROR_CRITICAL_EXECUTING_BLOCK + block.getAction() + ": " + t.getMessage() + 
                " Player: " + (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown");
            plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, t);
            
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
        
        if (conditionalBlock == null) {
            return null;
        }
        
        
        // Variable current initializer null is redundant
        // Removed redundant initializer
        CodeBlock current;
        
        
        if (!conditionalBlock.getChildren().isEmpty()) {
            
            current = conditionalBlock.getChildren().get(conditionalBlock.getChildren().size() - 1);
            
            
            while (current != null && current.getNextBlock() != null) {
                current = current.getNextBlock();
            }
        } else {
            
            current = conditionalBlock.getNextBlock();
            
            
            while (current != null && current.getNextBlock() != null && !"else".equals(current.getAction())) {
                current = current.getNextBlock();
            }
        }
        
        
        if (current != null && "else".equals(current.getAction())) {
            return current;
        }
        
        
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
        
        plugin.getLogger().info("Processing block chain at recursion depth " + recursionDepth + 
                              " with block action: " + (block != null ? block.getAction() : "null"));
        
        
        if (block == null || recursionDepth > MAX_RECURSION_DEPTH) {
            if (recursionDepth > MAX_RECURSION_DEPTH) {
                String errorMsg = ERROR_MAX_RECURSION_EXCEEDED_IN_WHILE_LOOP + " (depth: " + recursionDepth + ")";
                plugin.getLogger().warning(errorMsg + " Player: " + 
                    (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                return ExecutionResult.error(errorMsg);
            }
            return ExecutionResult.success(Constants.BLOCK_IS_NULL);
        }

        
        executionChain.add(block);
        
        
        if (context.isCancelled()) {
            return ExecutionResult.success("Execution cancelled");
        }

        
        long startTime = System.currentTimeMillis();
        if (startTime - context.getStartTime() > maxExecutionTimeMs) {
            String errorMsg = "Script execution timed out after " + maxExecutionTimeMs + "ms";
            plugin.getLogger().warning(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            return ExecutionResult.error(errorMsg);
        }

        try {
            
            ExecutionResult result = processBlock(block, context, recursionDepth);
            
            
            switch (getResultType(result)) {
                case PAUSE:
                    
                    plugin.getLogger().info("Block execution paused for " + result.getPauseTicks() + " ticks in chain");
                    return result;
                case AWAIT:
                    
                    plugin.getLogger().info("Block execution awaiting CompletableFuture in chain");
                    return result;
                case NORMAL:
                default:
                    
                    break;
            }
            
            
            if (result.isTerminated()) {
                plugin.getLogger().info("Block execution terminated");
                return result;
            }
            
            
            // Condition result != null is always true
            // Removed redundant null check since we already check for null above
            if (!result.isSuccess()) {
                plugin.getLogger().warning("Block execution failed: " + result.getMessage());
                return result;
            }
            
            
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock != null) {
                plugin.getLogger().info("Continuing to next block in chain with action: " + nextBlock.getAction());
                return processBlockChain(nextBlock, context, executionChain, recursionDepth + 1);
            }
            
            
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
        
        
        plugin.getLogger().info("Action registration via ScriptEngine is deprecated. Use @BlockMeta annotation instead.");
    }

    @Override
    public void registerCondition(BlockType type, BlockCondition condition) {
        
        
        plugin.getLogger().info("Condition registration via ScriptEngine is deprecated. Use @BlockMeta annotation instead.");
    }
    
    /**
     * Register an action with a specific ID
     * 
     * @param actionId The action ID
     * @param action The BlockAction implementation
     */
    public void registerAction(String actionId, BlockAction action) {
        
        plugin.getLogger().info("Action registration via ScriptEngine is deprecated. Use @BlockMeta annotation instead.");
    }
    
    /**
     * Register a condition with a specific ID
     * 
     * @param conditionId The condition ID
     * @param condition The BlockCondition implementation
     */
    public void registerCondition(String conditionId, BlockCondition condition) {
        
        plugin.getLogger().info("Condition registration via ScriptEngine is deprecated. Use @BlockMeta annotation instead.");
    }
    
    @Override
    public BlockType getBlockType(Material material, String actionName) {
        
        
        if (material != null && actionName != null) {
            
            java.util.List<BlockConfigService.BlockConfig> configs = blockConfigService.getBlockConfigsForMaterial(material);
            
            BlockType blockType = findBlockTypeInConfigs(configs, actionName);
            if (blockType != null) {
                return blockType;
            }
        }
        
        
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
        
        
        if (block == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success(Constants.BLOCK_IS_NULL));
        }
        
        
        CodeScript tempScript = new CodeScript(block);
        
        
        return advancedExecutionEngine.executeScript(tempScript, player, mode, priority, trigger);
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeScriptDelayed(CodeScript script, Player player, 
                                                                 long delayTicks, String trigger) {
        
        return advancedExecutionEngine.executeScript(script, player, 
            AdvancedExecutionEngine.ExecutionMode.DELAYED, 
            AdvancedExecutionEngine.Priority.NORMAL, 
            trigger);
    }
    

    
    
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
        
        return activeExecutions.size() > 50 || 
               advancedExecutionEngine.getStatistics().getActiveThreads() >= 4; 
    }
    
    /**
     * Get the current throughput (executions per second)
     * 
     * @return Current execution throughput
     */
    @Override
    public double getCurrentThroughput() {
        
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
    
    /**
     * Executes a WHILE loop
     * 
     * @param block The while loop block
     * @param context The execution context
     * @param recursionDepth The current recursion depth
     * @return The execution result
     */
    private ExecutionResult executeWhileLoop(CodeBlock block, ExecutionContext context, int recursionDepth) {
        try {
            
            if (recursionDepth > MAX_RECURSION_DEPTH) {
                String errorMsg = ERROR_MAX_RECURSION_EXCEEDED_IN_WHILE_LOOP + " (depth: " + recursionDepth + ")";
                plugin.getLogger().warning(errorMsg + " Player: " + 
                    (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                return ExecutionResult.error(errorMsg);
            }
            
            
            Object conditionIdObj = context.getVariable("_while_condition");
            Object maxIterationsObj = context.getVariable("_while_max_iterations");
            Object iterationCountObj = context.getVariable("_while_iteration_count");
            
            if (!(conditionIdObj instanceof String) || !(maxIterationsObj instanceof Integer) || !(iterationCountObj instanceof Integer)) {
                return ExecutionResult.error("Invalid while loop context");
            }
            
            String conditionId = (String) conditionIdObj;
            int maxIterations = (Integer) maxIterationsObj;
            int iterationCount = (Integer) iterationCountObj;
            
            
            if (iterationCount >= maxIterations) {
                plugin.getLogger().info("While loop reached maximum iterations: " + maxIterations);
                return ExecutionResult.success("While loop completed");
            }
            
            
            BlockCondition conditionHandler = conditionFactory.createCondition(conditionId);
            if (conditionHandler == null) {
                return ExecutionResult.error("Condition handler not found for: " + conditionId);
            }
            
            boolean conditionResult = conditionHandler.evaluate(block, context);
            plugin.getLogger().fine("While loop condition " + conditionId + " evaluated to " + conditionResult);
            
            if (conditionResult) {
                
                plugin.getLogger().info("While loop condition is true, executing loop body (iteration " + iterationCount + ")");
                
                
                context.setVariable("_while_iteration_count", iterationCount + 1);
                
                
                if (!block.getChildren().isEmpty()) {
                    CodeBlock loopBody = block.getChildren().get(0);
                    
                    
                    ExecutionResult bodyResult = processBlock(loopBody, context, recursionDepth + 1);
                    
                    
                    if (bodyResult.isTerminated()) {
                        if (bodyResult.getMessage().contains("BREAK")) {
                            plugin.getLogger().info("While loop terminated by BREAK statement");
                            return ExecutionResult.success("While loop terminated by BREAK");
                        } else if (bodyResult.getMessage().contains("CONTINUE")) {
                            plugin.getLogger().info("While loop iteration skipped by CONTINUE statement");
                            
                        }
                    }
                    
                    
                    return executeWhileLoop(block, context, recursionDepth);
                } else {
                    
                    return executeWhileLoop(block, context, recursionDepth);
                }
            } else {
                
                plugin.getLogger().info("While loop condition is false, exiting loop");
                return ExecutionResult.success("While loop completed");
            }
        } catch (Exception e) {
            String errorMsg = "Error executing while loop: " + e.getMessage();
            plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, e);
            return ExecutionResult.error(errorMsg);
        }
    }
    
    /**
     * Executes a FOR EACH loop
     * 
     * @param block The for-each loop block
     * @param context The execution context
     * @param recursionDepth The current recursion depth
     * @return The execution result
     */
    private ExecutionResult executeForEachLoop(CodeBlock block, ExecutionContext context, int recursionDepth) {
        try {
            
            if (recursionDepth > MAX_RECURSION_DEPTH) {
                String errorMsg = ERROR_MAX_RECURSION_EXCEEDED_IN_WHILE_LOOP + " (depth: " + recursionDepth + ")";
                plugin.getLogger().warning(errorMsg + " Player: " + 
                    (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                return ExecutionResult.error(errorMsg);
            }
            
            
            Object collectionNameObj = context.getVariable("_foreach_collection");
            Object variableNameObj = context.getVariable("_foreach_variable");
            Object indexObj = context.getVariable("_foreach_index");
            
            if (!(collectionNameObj instanceof String) || !(variableNameObj instanceof String) || !(indexObj instanceof Integer)) {
                return ExecutionResult.error("Invalid for-each loop context");
            }
            
            String collectionName = (String) collectionNameObj;
            String variableName = (String) variableNameObj;
            int index = (Integer) indexObj;
            
            
            VariableManager variableManager = getVariableManager();
            if (variableManager == null || context.getPlayer() == null) {
                return ExecutionResult.error("Variable manager or player not available");
            }
            
            DataValue collectionValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), collectionName);
            if (collectionValue == null || !(collectionValue instanceof ListValue)) {
                return ExecutionResult.error("Collection not found or not a list: " + collectionName);
            }
            
            ListValue listValue = (ListValue) collectionValue;
            List<DataValue> items = (List<DataValue>) listValue.getValue();
            
            
            if (index >= items.size()) {
                plugin.getLogger().info("For-each loop completed, processed " + index + " items");
                return ExecutionResult.success("For-each loop completed");
            }
            
            
            DataValue currentItem = items.get(index);
            
            
            variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), variableName, currentItem);
            
            
            context.setVariable("_foreach_index", index + 1);
            
            
            plugin.getLogger().info("For-each loop processing item " + index + " of " + items.size());
            
            
            if (!block.getChildren().isEmpty()) {
                CodeBlock loopBody = block.getChildren().get(0);
                
                
                ExecutionResult bodyResult = processBlock(loopBody, context, recursionDepth + 1);
                
                
                if (bodyResult.isTerminated()) {
                    if (bodyResult.getMessage().contains("BREAK")) {
                        plugin.getLogger().info("For-each loop terminated by BREAK statement");
                        return ExecutionResult.success("For-each loop terminated by BREAK");
                    } else if (bodyResult.getMessage().contains("CONTINUE")) {
                        plugin.getLogger().info("For-each loop iteration skipped by CONTINUE statement");
                        
                    }
                }
                
                
                return executeForEachLoop(block, context, recursionDepth);
            } else {
                
                return executeForEachLoop(block, context, recursionDepth);
            }
        } catch (Exception e) {
            String errorMsg = "Error executing for-each loop: " + e.getMessage();
            plugin.getLogger().log(java.util.logging.Level.SEVERE, errorMsg, e);
            return ExecutionResult.error(errorMsg);
        }
    }
}