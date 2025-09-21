package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.cache.BlockExecutionCache;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.services.BlockConfigService;
import java.util.HashMap;
import java.util.Map;
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
import java.util.Map;

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
        this.executionCache = new BlockExecutionCache(5, TimeUnit.MINUTES, 1000);
        
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
                    plugin.getLogger().severe("Script execution error: " + e.getMessage());
                    e.printStackTrace();
                    future.completeExceptionally(e);
                } catch (Throwable t) {
                    plugin.getLogger().severe("Critical script execution error: " + t.getMessage());
                    t.printStackTrace();
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
                    ExecutionResult result = processBlock(block, context, 0);
                    future.complete(result);
                } catch (Exception e) {
                    plugin.getLogger().severe("Block execution error: " + e.getMessage());
                    e.printStackTrace();
                    future.completeExceptionally(e);
                } catch (Throwable t) {
                    plugin.getLogger().severe("Critical block execution error: " + t.getMessage());
                    t.printStackTrace();
                    future.completeExceptionally(new RuntimeException(Constants.CRITICAL_EXECUTION_ERROR, t));
                }
            }
        }.runTask(plugin);

        return future;
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlockChain(CodeBlock startBlock, Player player, String trigger) {
        if (startBlock == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success(Constants.START_BLOCK_IS_NULL));
        }

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
                    ExecutionResult result = processBlock(startBlock, context, 0);
                    future.complete(result);
                } catch (Exception e) {
                    plugin.getLogger().severe("Block chain execution error: " + e.getMessage());
                    e.printStackTrace();
                    future.completeExceptionally(e);
                } catch (Throwable t) {
                    plugin.getLogger().severe("Critical block chain execution error: " + t.getMessage());
                    t.printStackTrace();
                    future.completeExceptionally(new RuntimeException(Constants.CRITICAL_EXECUTION_ERROR, t));
                }
            }
        }.runTask(plugin);

        return future;

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
        
        // ... (rest of the processBlock method remains the same)

        try {
            // ... (rest of the try block remains the same)

            // --- ОСНОВНАЯ ЛОГИКА ---
            switch (blockType) {
                // ... (rest of the switch block remains the same)

                default:
                    String errorMsg = ERROR_UNSUPPORTED_BLOCK_TYPE + blockType;
                    plugin.getLogger().warning(errorMsg + " Player: " + 
                        (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                    // Cache the result before returning
                    if (result != null && result.isSuccessful() && isCacheable(block)) {
                        executionCache.put(block, cacheContext, result);
                    }
                    
                    return processBlock(block.getNextBlock(), context, recursionDepth + 1);
            }
        } catch (Exception e) {
            String errorMsg = ERROR_EXECUTING_BLOCK + block.getAction() + ": " + e.getMessage();
            plugin.getLogger().severe(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            e.printStackTrace();
            // Cache the error result
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, ExecutionResult.error(errorMsg));
            }
            return ExecutionResult.error(errorMsg);
        } catch (Throwable t) {
            String errorMsg = ERROR_CRITICAL_EXECUTING_BLOCK + block.getAction() + ": " + t.getMessage();
            plugin.getLogger().severe(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            t.printStackTrace();
            // Cache the error result
            if (isCacheable(block)) {
                executionCache.put(block, cacheContext, ExecutionResult.error(errorMsg));
            }
            return ExecutionResult.error(errorMsg);
        }
    }
    
    // ... (rest of the code remains the same)
        }
        
        return lastResult;
    }
    
    @Override
    public void registerAction(BlockType type, BlockAction action) {
        // Implementation for registering actions
        // This would typically involve adding to an internal registry
        if (actionFactory != null) {
            //actionFactory.registerAction(type.name(), action);
        }
    }

    @Override
    public void registerCondition(BlockType type, BlockCondition condition) {
        // Implementation for registering conditions
        // This would typically involve adding to an internal registry
        if (conditionFactory != null) {
            //conditionFactory.registerCondition(type.name(), condition);
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