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
    }

    // Add a counter for instruction limiting to prevent infinite loops
    private static final int MAX_INSTRUCTIONS_PER_TICK = 1000;
    
    private ExecutionResult processBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
        // Check if execution is cancelled
        if (block == null || context.isCancelled()) {
            return ExecutionResult.success(Constants.END_OF_CHAIN_OR_CANCELLED);
        }
        
        // Check for pause/step conditions
        checkPauseAndBreakpoints(block, context);
        
        // Check for instruction limit to prevent infinite loops
        if (context.getInstructionCount() > MAX_INSTRUCTIONS_PER_TICK) {
            String errorMsg = Constants.MAX_INSTRUCTIONS_EXCEEDED;
            plugin.getLogger().warning(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            return ExecutionResult.error(errorMsg);
        }
        
        // Increment instruction counter
        context.incrementInstructionCount();
        
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            String errorMsg = Constants.MAX_RECURSION_EXCEEDED;
            plugin.getLogger().warning(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            return ExecutionResult.error(errorMsg);
        }
        
        if (debugger.isDebugging(context.getPlayer())) {
            debugger.onBlockExecute(context.getPlayer(), block, context.getBlockLocation());
        }

        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(block.getAction());
        if (config == null) {
            String errorMsg = Constants.UNKNOWN_BLOCK_ACTION + block.getAction();
            plugin.getLogger().warning(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            return ExecutionResult.error(errorMsg);
        }
        
        String blockType = config.getType();
        
        try {
            // --- ОСНОВНАЯ ЛОГИКА ---
            switch (blockType) {
                case BLOCK_TYPE_EVENT:
                case BLOCK_TYPE_ACTION:
                    return processActionBlock(block, context, recursionDepth);
                    
                case BLOCK_TYPE_CONDITION:
                    return processConditionBlock(block, context, recursionDepth);
                    
                case BLOCK_TYPE_CONTROL:
                    return processControlBlock(block, context, recursionDepth);
                    
                case BLOCK_TYPE_FUNCTION:
                    return processFunctionBlock(block, context, recursionDepth);

                default:
                    String errorMsg = ERROR_UNSUPPORTED_BLOCK_TYPE + blockType;
                    plugin.getLogger().warning(errorMsg + " Player: " + 
                        (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                    return ExecutionResult.error(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = ERROR_EXECUTING_BLOCK + block.getAction() + ": " + e.getMessage();
            plugin.getLogger().severe(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            e.printStackTrace();
            return ExecutionResult.error(errorMsg);
        } catch (Throwable t) {
            String errorMsg = ERROR_CRITICAL_EXECUTING_BLOCK + block.getAction() + ": " + t.getMessage();
            plugin.getLogger().severe(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            t.printStackTrace();
            return ExecutionResult.error(errorMsg);
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
    
    /**
     * Process action blocks (EVENT and ACTION types)
     */
    private ExecutionResult processActionBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
        BlockAction action = actionFactory.createAction(block.getAction());
        if (action == null) {
            String errorMsg = ERROR_ACTION_HANDLER_NOT_FOUND + block.getAction();
            plugin.getLogger().warning(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            return ExecutionResult.error(errorMsg);
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
    }
    
    /**
     * Process condition blocks
     */
    private ExecutionResult processConditionBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
        BlockCondition condition = conditionFactory.createCondition(block.getAction());
        if (condition == null) {
            String errorMsg = ERROR_CONDITION_HANDLER_NOT_FOUND + block.getAction();
            plugin.getLogger().warning(errorMsg + " Player: " + 
                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
            return ExecutionResult.error(errorMsg);
        }
        boolean conditionResult = condition.evaluate(block, context);
        context.setLastConditionResult(conditionResult);
        if (debugger.isDebugging(context.getPlayer())) {
            debugger.onConditionResult(context.getPlayer(), block, conditionResult);
        }
        // Сохраняем результат условия для последующего использования в блоках CONTROL
        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
    }
    
    /**
     * Process control blocks
     */
    private ExecutionResult processControlBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
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
            case CONTROL_ACTION_CONDITIONAL_BRANCH:
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
                
            case CONTROL_ACTION_ELSE:
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
                
            case CONTROL_ACTION_WHILE_LOOP:
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
                            String errorMsg = ERROR_MAX_RECURSION_EXCEEDED_IN_WHILE_LOOP;
                            plugin.getLogger().warning(errorMsg + " Player: " + 
                                (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                            return ExecutionResult.error(errorMsg);
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
                                String errorMsg = ERROR_MAX_RECURSION_EXCEEDED_IN_WHILE_LOOP;
                                plugin.getLogger().warning(errorMsg + " Player: " + 
                                    (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                                return ExecutionResult.error(errorMsg);
                            }
                        }
                    }
                    // After body execution, loop back to the same while block to check condition again
                    // But limit recursion to prevent stack overflow
                    if (recursionDepth < MAX_RECURSION_DEPTH - 1) {
                        return processBlock(block, context, recursionDepth + 1);
                    } else {
                        String errorMsg = ERROR_MAX_RECURSION_EXCEEDED_IN_WHILE_LOOP;
                        plugin.getLogger().warning(errorMsg + " Player: " + 
                            (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"));
                        return ExecutionResult.error(errorMsg);
                    }
                }
                // Condition is false, exit loop and continue with next block
                return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                
            case CONTROL_ACTION_FOR_EACH:
                // For each implementation with break/continue support
                DataValue listValue = block.getParameter("list");
                String itemVariable = block.getParameter("itemVariable").asString();
                
                if (listValue != null && listValue instanceof ListValue && itemVariable != null) {
                    ListValue list = (ListValue) listValue;
                    VariableManager variableManager = getVariableManager();
                    
                    // Check if player is available
                    if (context.getPlayer() == null) {
                        String errorMsg = ERROR_PLAYER_REQUIRED_FOR_FOREACH;
                        plugin.getLogger().warning(errorMsg);
                        return ExecutionResult.error(errorMsg);
                    }
                    
                    UUID playerId = context.getPlayer().getUniqueId();
                    
                    // Store original variable value
                    DataValue originalValue = variableManager.getPlayerVariable(playerId, itemVariable);
                    
                    try {
                        // Iterate through list items
                        for (DataValue item : list.getValues()) {
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
                                continue;
                            }
                            
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
                                    continue;
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
                
            case CONTROL_ACTION_BREAK:
                // Break implementation - set a flag in context to break out of loops
                context.setBreakFlag(true);
                if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage("§aBreak statement executed");
                }
                return ExecutionResult.success("Break executed");
                
            case CONTROL_ACTION_CONTINUE:
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
    }
    
    /**
     * Process function blocks
     */
    private ExecutionResult processFunctionBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
        // Enhanced function processing with actual implementation
        String functionName = block.getAction();
        
        // Handle function call action
        if (FUNCTION_ACTION_CALL_FUNCTION.equals(functionName)) {
            // Get function name parameter
            DataValue functionNameValue = block.getParameter("functionName");
            if (functionNameValue != null && !functionNameValue.isEmpty()) {
                String funcName = functionNameValue.asString();
                
                // Get function manager
                com.megacreative.coding.functions.AdvancedFunctionManager functionManager = 
                    plugin.getServiceRegistry().getAdvancedFunctionManager();
                
                if (functionManager != null) {
                    try {
                        // Prepare function arguments from block parameters
                        java.util.List<com.megacreative.coding.values.DataValue> arguments = new java.util.ArrayList<>();
                        
                        // Collect all parameters that start with "arg_" or are numbered
                        for (String paramName : block.getParameters().keySet()) {
                            if (paramName.startsWith("arg_") || paramName.matches("arg\\d+")) {
                                arguments.add(block.getParameter(paramName));
                            }
                        }
                        
                        // Execute the function
                        java.util.concurrent.CompletableFuture<ExecutionResult> futureResult = 
                            functionManager.executeFunction(funcName, context.getPlayer(), arguments.toArray(new com.megacreative.coding.values.DataValue[0]));
                        ExecutionResult functionResult = futureResult.get();
                        
                        // Process the result
                        if (functionResult.isTerminated()) {
                            // Preserve termination status and return value
                            ExecutionResult functionCallResult = ExecutionResult.success("Function call completed with return: " + funcName);
                            functionCallResult.setTerminated(true);
                            if (functionResult.getReturnValue() != null) {
                                functionCallResult.setReturnValue(functionResult.getReturnValue());
                                // Store return value in context for use by subsequent blocks
                                context.setVariable("last_function_return", functionResult.getReturnValue());
                            }
                            return functionCallResult; // Stop execution and return the result
                        }
                        
                        // Handle successful function execution without termination
                        if (functionResult.isSuccess()) {
                            // Store return value in context for use by subsequent blocks
                            if (functionResult.getReturnValue() != null) {
                                context.setVariable("last_function_return", functionResult.getReturnValue());
                            }
                            // Continue with next block
                            return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                        } else {
                            // Return error
                            return functionResult;
                        }
                    } catch (Exception e) {
                        String errorMsg = ERROR_EXECUTING_FUNCTION + funcName + ": " + e.getMessage();
                        plugin.getLogger().severe(errorMsg);
                        return ExecutionResult.error(errorMsg);
                    }
                } else {
                    String errorMsg = ERROR_FUNCTION_MANAGER_NOT_AVAILABLE;
                    plugin.getLogger().warning(errorMsg);
                    return ExecutionResult.error(errorMsg);
                }
            }
        }
        // For other function-related actions, just go to the next block
        return processBlock(block.getNextBlock(), context, recursionDepth + 1);
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