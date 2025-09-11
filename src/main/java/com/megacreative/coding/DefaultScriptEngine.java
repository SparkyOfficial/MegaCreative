package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DefaultScriptEngine implements ScriptEngine {
    
    private final MegaCreative plugin;
    private final VariableManager variableManager;
    private final VisualDebugger debugger;
    private final BlockConfigService blockConfigService;
    private final ActionFactory actionFactory;
    private final ConditionFactory conditionFactory;
    
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    private static final int MAX_RECURSION_DEPTH = 100;

    public DefaultScriptEngine(MegaCreative plugin, VariableManager variableManager, VisualDebugger debugger,
                               BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        this.debugger = debugger;
        this.blockConfigService = blockConfigService;
        // Передаем DependencyContainer, если он у вас есть, или создаем новые
        this.actionFactory = new ActionFactory(plugin.getDependencyContainer());
        this.conditionFactory = new ConditionFactory();
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
                // Here will be logic for IF/ELSE, LOOP etc.
                switch (block.getAction()) {
                    case "conditionalBranch":
                        if (context.getLastConditionResult()) {
                            // The result of the last condition was TRUE, execute child blocks
                            if (!block.getChildren().isEmpty()) {
                                // Execute the first child branch
                                ExecutionResult childResult = processBlock(block.getChildren().get(0), context, recursionDepth + 1);
                                // If the child branch ended with an error, stop
                                if (!childResult.isSuccess()) return childResult;
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
                            }
                        }
                        // After ELSE we go to the next block in the MAIN chain
                        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                        
                    case "whileLoop":
                        // While loop implementation
                        // Check condition first
                        BlockCondition whileCondition = conditionFactory.createCondition(block.getCondition());
                        if (whileCondition != null && whileCondition.evaluate(block, context)) {
                            // Execute body
                            if (!block.getChildren().isEmpty()) {
                                ExecutionResult childResult = processBlock(block.getChildren().get(0), context, recursionDepth + 1);
                                if (!childResult.isSuccess()) return childResult;
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
                        // For each implementation would go here
                        // This is a placeholder for future implementation
                        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                        
                    case "break":
                        // Break implementation - would need to track loop context
                        // This is a placeholder for future implementation
                        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                        
                    case "continue":
                        // Continue implementation - would need to track loop context
                        // This is a placeholder for future implementation
                        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                        
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
}