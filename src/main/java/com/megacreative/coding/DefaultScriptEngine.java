package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.VariableManagerImpl;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the ScriptEngine interface.
 * Combines the best features of ScriptExecutor and ExecutorEngine
 * with a focus on performance and extensibility.
 */
public class DefaultScriptEngine implements ScriptEngine {
    
    private final MegaCreative plugin;
    private final VariableManager variableManager;
    private final VisualDebugger debugger;
    
    // Registry for actions and conditions
    private final Map<BlockType, BlockAction> actionRegistry = new ConcurrentHashMap<>();
    private final Map<BlockType, BlockCondition> conditionRegistry = new ConcurrentHashMap<>();
    
    // Active execution tracking
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    
    public DefaultScriptEngine(MegaCreative plugin) {
        this.plugin = plugin;
        this.variableManager = new VariableManagerImpl(plugin);
        this.debugger = new VisualDebugger(plugin);
        
        // Initialize default actions and conditions
        registerDefaultActions();
        registerDefaultConditions();
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, String trigger) {
        String executionId = UUID.randomUUID().toString();
        ExecutionContext context = new ExecutionContext(executionId, script, player, trigger);
        activeExecutions.put(executionId, context);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Process the starting block
                CodeBlock startBlock = script.getStartBlock();
                if (startBlock != null) {
                    processBlock(startBlock, context);
                    return ExecutionResult.success("Script executed successfully");
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
    private void processBlock(CodeBlock block, ExecutionContext context) {
        if (block == null || context.isCancelled()) {
            return;
        }

        // Handle pausing for debugging
        if (context.isPaused() && !context.isStepping()) {
            synchronized (context) {
                try {
                    context.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        // Reset step flag after processing one block
        if (context.isStepping()) {
            context.setPaused(true);
            context.setStepping(false);
        }

        try {
            // Visual feedback
            if (context.getPlayer() != null) {
                debugger.highlightBlockExecution(context.getPlayer(), block.getLocation(), block);
            }

            // Process the current block
            BlockAction action = actionRegistry.get(block.getType());
            if (action != null) {
                action.execute(block, context);
            } else {
                plugin.getLogger().warning("No action registered for block type: " + block.getType());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error processing block: " + e.getMessage());
            e.printStackTrace();
            
            // Visual feedback for error
            if (context.getPlayer() != null) {
                debugger.highlightError(context.getPlayer(), block.getLocation(), e.getMessage());
            }
        }
    }
    
    // Helper methods for execution
    
    private void registerDefaultActions() {
        // Register core actions
        registerAction(BlockType.ACTION_MESSAGE, new MessageAction());
        registerAction(BlockType.ACTION_TELEPORT, new TeleportAction());
        registerAction(BlockType.ACTION_GIVE_ITEM, new GiveItemAction());
        registerAction(BlockType.ACTION_EFFECT, new EffectAction());
        registerAction(BlockType.ACTION_WAIT, new WaitAction());
        registerAction(BlockType.ACTION_REPEAT, new RepeatAction());
        registerAction(BlockType.ACTION_IF, new IfAction());
        registerAction(BlockType.ACTION_SET_VARIABLE, new SetVariableAction());
        
        // Register more actions as needed
    }
    
    private void registerDefaultConditions() {
        // Register core conditions
        registerCondition(BlockType.CONDITION_IS_OP, new IsOpCondition());
        registerCondition(BlockType.CONDITION_HAS_ITEM, new HasItemCondition());
        registerCondition(BlockType.CONDITION_HAS_PERMISSION, new HasPermissionCondition());
        registerCondition(BlockType.CONDITION_IS_IN_WORLD, new IsInWorldCondition());
        registerCondition(BlockType.CONDITION_COMPARE_VARIABLE, new CompareVariableCondition());
        
        // Register more conditions as needed
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
}
