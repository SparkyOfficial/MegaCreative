package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.services.BlockConfigService;
// 🎆 Reference system-style advanced execution
import com.megacreative.coding.executors.AdvancedExecutionEngine;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DefaultScriptEngine implements ScriptEngine, EnhancedScriptEngine {
    
    private final MegaCreative plugin;
    private final VariableManager variableManager;
    private final VisualDebugger debugger;
    private final BlockConfigService blockConfigService;
    private final ActionFactory actionFactory;
    private final ConditionFactory conditionFactory;
    
    // 🎆 Reference system-style advanced execution engine
    private final AdvancedExecutionEngine advancedExecutionEngine;
    
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    private static final int MAX_RECURSION_DEPTH = 100;
    
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
    }
    
    public void initialize() {
        // Initialize the script engine with any required setup
        // This method can be used to register built-in actions and conditions
        // Register all actions from BlockConfigService
        for (BlockConfigService.BlockConfig config : blockConfigService.getAllBlockConfigs()) {
            if ("ACTION".equals(config.getType())) {
                actionFactory.registerAction(config.getId(), config.getDisplayName());
            } else if ("CONDITION".equals(config.getType())) {
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
            return CompletableFuture.completedFuture(ExecutionResult.success("Script is invalid or disabled."));
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
                    future.completeExceptionally(e);
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
            return CompletableFuture.completedFuture(ExecutionResult.success("Block is null."));
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
                    future.completeExceptionally(e);
                }
            }
        }.runTask(plugin);

        return future;
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlockChain(CodeBlock startBlock, Player player, String trigger) {
        if (startBlock == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success("Start block is null."));
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
                    future.completeExceptionally(e);
                }
            }
        }.runTask(plugin);

        return future;
    }

    // Add a counter for instruction limiting to prevent infinite loops
    private static final int MAX_INSTRUCTIONS_PER_TICK = 1000;
    
    private ExecutionResult processBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
        // Check if execution is cancelled
        if (block == null || context.isCancelled()) {
            return ExecutionResult.success("End of chain or cancelled.");
        }
        
        // Check for pause/step conditions
        checkPauseAndBreakpoints(block, context);
        
        // Check for instruction limit to prevent infinite loops
        if (context.getInstructionCount() > MAX_INSTRUCTIONS_PER_TICK) {
            return ExecutionResult.error("Max instructions per tick exceeded. Possible infinite loop detected.");
        }
        
        // Increment instruction counter
        context.incrementInstructionCount();
        
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            return ExecutionResult.error("Max recursion depth exceeded.");
        }
        if (debugger.isDebugging(context.getPlayer())) {
            debugger.onBlockExecute(context.getPlayer(), block, context.getBlockLocation());
        }

        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(block.getAction());
        if (config == null) {
            return ExecutionResult.error("Unknown block action ID: " + block.getAction());
        }
        
        String blockType = config.getType();
        
        // --- ОСНОВНАЯ ЛОГИКА ---
        switch (blockType) {
            case "EVENT":
            case "ACTION":
                BlockAction action = actionFactory.createAction(block.getAction());
                if (action == null) {
                    return ExecutionResult.error("Action handler not found for: " + block.getAction());
                }
                ExecutionResult result = action.execute(block, context);
                // Если действие прошло успешно, переходим к следующему блоку
                if (result.isSuccess()) {
                    // Check if execution was terminated (e.g., by a return statement)
                    if (result.isTerminated()) {
                        return result; // Stop execution and return the result
                    }
                    return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                }
                return result; // Если была ошибка, останавливаемся
                
            case "CONDITION":
                BlockCondition condition = conditionFactory.createCondition(block.getAction());
                if (condition == null) {
                    return ExecutionResult.error("Condition handler not found for: " + block.getAction());
                }
                boolean conditionResult = condition.evaluate(block, context);
                context.setLastConditionResult(conditionResult);
                if (debugger.isDebugging(context.getPlayer())) {
                    debugger.onConditionResult(context.getPlayer(), block, conditionResult);
                }
                // Сохраняем результат условия для последующего использования в блоках CONTROL
                return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                
            case "CONTROL":
                // === BRACKET LOGIC ===
                if (block.isBracket()) {
                    if (block.getBracketType() == CodeBlock.BracketType.OPEN) {
                        // This is an opening bracket {
                        // All logic inside it should be executed only if the last condition was true
                        if (context.getLastConditionResult()) {
                            // Execute all child elements
                            for (CodeBlock child : block.getChildren()) {
                                ExecutionResult childResult = processBlock(child, context, recursionDepth + 1);
                                if (!childResult.isSuccess()) {
                                    return childResult; // Stop on first error
                                }
                                // Check if execution was terminated (e.g., by a return statement)
                                if (childResult.isTerminated()) {
                                    return childResult; // Stop execution and return the result
                                }
                            }
                        }
                        // After executing the bracket (or skipping it), go to the NEXT block AFTER this bracket
                        return processBlock(findNextBlockAfterBracket(block), context, recursionDepth + 1);
                    } else {
                        // Closing bracket } - should not be processed directly
                        // This is handled by the opening bracket logic
                        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                    }
                }
                
                // === TRADITIONAL CONTROL LOGIC ===
                // Implementing logic for IF/ELSE, LOOP etc.
                switch (block.getAction()) {
                    case "conditionalBranch":
                        if (context.getLastConditionResult()) {
                            // The result of the last condition was TRUE, execute child blocks
                            if (!block.getChildren().isEmpty()) {
                                // Execute the first child branch
                                ExecutionResult childResult = processBlock(block.getChildren().get(0), context, recursionDepth + 1);
                                // If the child branch ended with an error, stop
                                if (!childResult.isSuccess()) return childResult;
                                // Check if execution was terminated (e.g., by a return statement)
                                if (childResult.isTerminated()) {
                                    return childResult; // Stop execution and return the result
                                }
                            }
                        }
                        // Regardless of the result, after IF we go to the next block in the MAIN chain
                        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                        
                    case "else":
                        // Processing the ELSE block
                        if (!context.getLastConditionResult()) {
                            // The previous condition was FALSE, execute the ELSE block
                            if (!block.getChildren().isEmpty()) {
                                // Execute the first child branch (else body)
                                ExecutionResult childResult = processBlock(block.getChildren().get(0), context, recursionDepth + 1);
                                // If the child branch ended with an error, stop
                                if (!childResult.isSuccess()) return childResult;
                                // Check if execution was terminated (e.g., by a return statement)
                                if (childResult.isTerminated()) {
                                    return childResult; // Stop execution and return the result
                                }
                            }
                        }
                        // After ELSE we go to the next block in the MAIN chain
                        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                        
                    case "whileLoop":
                        // While loop implementation
                        // Check condition first
                        BlockCondition whileCondition = conditionFactory.createCondition(block.getCondition());
                        if (whileCondition != null && whileCondition.evaluate(block, context)) {
                            // Check for break flag before executing body
                            if (context.hasBreakFlag()) {
                                context.clearBreakFlag();
                                // Exit loop and continue with next block
                                return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                            }
                            
                            // Check for continue flag
                            if (context.hasContinueFlag()) {
                                context.clearContinueFlag();
                                // Skip to next iteration without executing body
                                if (recursionDepth < MAX_RECURSION_DEPTH - 1) {
                                    return processBlock(block, context, recursionDepth + 1);
                                } else {
                                    return ExecutionResult.error("Max recursion depth exceeded in while loop.");
                                }
                            }
                            
                            // Execute body
                            if (!block.getChildren().isEmpty()) {
                                ExecutionResult childResult = processBlock(block.getChildren().get(0), context, recursionDepth + 1);
                                if (!childResult.isSuccess()) return childResult;
                                // Check if execution was terminated (e.g., by a return statement)
                                if (childResult.isTerminated()) {
                                    return childResult; // Stop execution and return the result
                                }
                                
                                // Check for break flag after executing body
                                if (context.hasBreakFlag()) {
                                    context.clearBreakFlag();
                                    // Exit loop and continue with next block
                                    return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                                }
                                
                                // Check for continue flag after executing body
                                if (context.hasContinueFlag()) {
                                    context.clearContinueFlag();
                                    // Skip to next iteration
                                    if (recursionDepth < MAX_RECURSION_DEPTH - 1) {
                                        return processBlock(block, context, recursionDepth + 1);
                                    } else {
                                        return ExecutionResult.error("Max recursion depth exceeded in while loop.");
                                    }
                                }
                            }
                            // After body execution, loop back to the same while block to check condition again
                            // But limit recursion to prevent stack overflow
                            if (recursionDepth < MAX_RECURSION_DEPTH - 1) {
                                return processBlock(block, context, recursionDepth + 1);
                            } else {
                                return ExecutionResult.error("Max recursion depth exceeded in while loop.");
                            }
                        }
                        // Condition is false, exit loop and continue with next block
                        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                        
                    case "forEach":
                        // For each implementation
                        DataValue listValue = block.getParameter("list");
                        String itemVariable = block.getParameter("itemVariable").asString();
                        
                        if (listValue != null && listValue instanceof ListValue && itemVariable != null) {
                            ListValue list = (ListValue) listValue;
                            VariableManager variableManager = getVariableManager();
                            
                            // Check if player is available
                            if (context.getPlayer() == null) {
                                return ExecutionResult.error("Player required for forEach loop");
                            }
                            
                            UUID playerId = context.getPlayer().getUniqueId();
                            
                            // Store original variable value
                            DataValue originalValue = variableManager.getPlayerVariable(playerId, itemVariable);
                            
                            try {
                                // Iterate through list items
                                for (DataValue item : list.getValues()) {
                                    // Set current item as variable
                                    variableManager.setPlayerVariable(playerId, itemVariable, item);
                                    
                                    // Execute child blocks
                                    if (!block.getChildren().isEmpty()) {
                                        ExecutionResult childResult = processBlock(block.getChildren().get(0), context, recursionDepth + 1);
                                        if (!childResult.isSuccess()) return childResult;
                                        // Check if execution was terminated (e.g., by a return statement)
                                        if (childResult.isTerminated()) {
                                            return childResult; // Stop execution and return the result
                                        }
                                    }
                                }
                            } finally {
                                // Restore original variable value
                                if (originalValue != null) {
                                    variableManager.setPlayerVariable(playerId, itemVariable, originalValue);
                                } else {
                                    variableManager.setPlayerVariable(playerId, itemVariable, null);
                                }
                            }
                        }
                        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                        
                    case "break":
                        // Break implementation - set a flag in context to break out of loops
                        context.setBreakFlag(true);
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§aBreak statement executed");
                        }
                        return ExecutionResult.success("Break executed");
                        
                    case "continue":
                        // Continue implementation - set a flag in context to continue loops
                        context.setContinueFlag(true);
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§aContinue statement executed");
                        }
                        return ExecutionResult.success("Continue executed");
                        
                    default:
                        // For other CONTROL blocks, just go to the next block
                        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                }
                
            case "FUNCTION":
                // Function processing - this is the next big step.
                // For now, just go to the next block.
                return processBlock(block.getNextBlock(), context, recursionDepth + 1);

            default:
                return ExecutionResult.error("Unsupported block type: " + blockType);
        }
    }
    
    /**
     * Checks for pause conditions and breakpoints
     */
    private void checkPauseAndBreakpoints(CodeBlock block, ExecutionContext context) {
        // Check if execution is paused
        while (context.isPaused() && !context.isCancelled()) {
            synchronized (context) {
                try {
                    context.wait(); // Wait until resumed
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        
        // Handle stepping
        if (context.isStepping()) {
            context.setStepping(false);
            context.setPaused(true);
        }
    }
    
    /**
     * Finds the next block after a bracket group
     * This method looks for the matching closing bracket and returns the block after it
     */
    private CodeBlock findNextBlockAfterBracket(CodeBlock openingBracket) {
        if (!openingBracket.isBracket() || openingBracket.getBracketType() != CodeBlock.BracketType.OPEN) {
            return openingBracket.getNextBlock(); // Not a bracket, just return next
        }
        
        // Simple approach: since we have proper parent-child relationships,
        // the next block after a bracket group should be the nextBlock of the opening bracket
        // This assumes that AutoConnectionManager properly sets up the connections
        return openingBracket.getNextBlock();
    }
    
    // New method to process a chain of blocks without returning to parent
    private ExecutionResult processBlockChain(CodeBlock startBlock, ExecutionContext context, int recursionDepth) {
        CodeBlock current = startBlock;
        ExecutionResult lastResult = ExecutionResult.success();
        
        while (current != null && !context.isCancelled()) {
            lastResult = processBlock(current, context, recursionDepth);
            if (!lastResult.isSuccess()) {
                break;
            }
            current = current.getNextBlock();
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
            for (BlockConfigService.BlockConfig config : configs) {
                if (actionName.equals(config.getId())) {
                    String type = config.getType();
                    if ("EVENT".equals(type)) return BlockType.EVENT;
                    if ("ACTION".equals(type)) return BlockType.ACTION;
                    if ("CONDITION".equals(type)) return BlockType.CONDITION;
                    if ("CONTROL".equals(type)) return BlockType.CONTROL;
                    if ("FUNCTION".equals(type)) return BlockType.FUNCTION;
                }
            }
        }
        
        // Fallback to old method if material is not used
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(actionName);
        if (config != null) {
            String type = config.getType();
            if ("EVENT".equals(type)) return BlockType.EVENT;
            if ("ACTION".equals(type)) return BlockType.ACTION;
            if ("CONDITION".equals(type)) return BlockType.CONDITION;
            if ("CONTROL".equals(type)) return BlockType.CONTROL;
            if ("FUNCTION".equals(type)) return BlockType.FUNCTION;
        }
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