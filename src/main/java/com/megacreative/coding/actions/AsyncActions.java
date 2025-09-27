package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Async actions that execute code blocks asynchronously without blocking the main thread
 * Includes loop controls, delayed execution, and proper cleanup to prevent memory leaks
 */
@BlockMeta(id = "asyncActions", displayName = "§bAsync Actions", type = BlockType.ACTION)
public class AsyncActions implements BlockAction {
    
    // Static tracking of active async tasks to prevent runaway executions
    private static final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();
    private static final int MAX_CONCURRENT_TASKS = 20; // Per player
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        try {
            // Get action type
            DataValue actionValue = block.getParameter("action");
            if (actionValue == null) {
                return ExecutionResult.error("Action parameter is required");
            }
            
            String action = resolver.resolve(context, actionValue).asString();
            
            switch (action.toLowerCase()) {
                case "execute":
                    return executeAsync(block, context, resolver, player);
                    
                case "loop":
                    return executeAsyncLoop(block, context, resolver, player);
                    
                case "delay":
                    return executeDelayed(block, context, resolver, player);
                    
                case "cancel":
                    return cancelAsyncTask(block, context, resolver, player);
                    
                default:
                    return ExecutionResult.error("Unknown async action: " + action);
            }
        } catch (Exception e) {
            player.sendMessage("§cError in async action: " + e.getMessage());
            if (context.isDebugMode()) {
                context.getPlugin().getLogger().log(Level.SEVERE, "Stack trace: ", e);
            }
            return ExecutionResult.error("Error in async action: " + e.getMessage());
        }
    }
    
    /**
     * Executes child blocks asynchronously
     */
    private ExecutionResult executeAsync(CodeBlock block, ExecutionContext context, ParameterResolver resolver, Player player) {
        try {
            // Check concurrent task limit
            UUID playerId = player.getUniqueId();
            if (countActiveTasks(playerId) >= MAX_CONCURRENT_TASKS) {
                player.sendMessage("§cToo many active async tasks! Maximum: " + MAX_CONCURRENT_TASKS);
                return ExecutionResult.error("Too many active async tasks");
            }
            
            // Run the task asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(context.getPlugin(), () -> {
                try {
                    // Execute child blocks
                    if (block.getChildren() != null) {
                        for (CodeBlock childBlock : block.getChildren()) {
                            // Execute the child block using action factory
                            executeChildBlock(context, childBlock);
                        }
                    }
                    
                    if (context.isDebugMode()) {
                        player.sendMessage("§7[DEBUG] Async execution completed");
                    }
                } catch (Exception e) {
                    player.sendMessage("§cError in async execution: " + e.getMessage());
                    if (context.isDebugMode()) {
                        context.getPlugin().getLogger().log(Level.SEVERE, "Stack trace: ", e);
                    }
                }
            });
            
            return ExecutionResult.success("Async execution started");
        } catch (Exception e) {
            return ExecutionResult.error("Error starting async execution: " + e.getMessage());
        }
    }
    
    /**
     * Executes child blocks in a loop asynchronously
     */
    private ExecutionResult executeAsyncLoop(CodeBlock block, ExecutionContext context, ParameterResolver resolver, Player player) {
        try {
            // Resolve loop parameters
            DataValue iterationsValue = block.getParameter("iterations");
            DataValue delayValue = block.getParameter("delay");
            
            int iterations = iterationsValue != null ? 
                resolver.resolve(context, iterationsValue).asNumber().intValue() : -1; // -1 for infinite
            long delayTicks = delayValue != null ? 
                resolver.resolve(context, delayValue).asNumber().longValue() : 20L; // 1 second default
            
            // Safety validation
            if (iterations != -1) {
                iterations = Math.max(1, Math.min(iterations, 1000)); // Max 1000 iterations
            }
            delayTicks = Math.max(1, Math.min(delayTicks, 1200L)); // 1 tick to 1 minute max
            
            // Check concurrent task limit
            UUID playerId = player.getUniqueId();
            if (countActiveTasks(playerId) >= MAX_CONCURRENT_TASKS) {
                player.sendMessage("§cToo many active async tasks! Maximum: " + MAX_CONCURRENT_TASKS);
                return ExecutionResult.error("Too many active async tasks");
            }
            
            // Create and schedule the async loop task
            AsyncLoopTask loopTask = new AsyncLoopTask(context, block, iterations);
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(context.getPlugin(), loopTask, delayTicks, delayTicks);
            
            // Track the task for cleanup
            activeTasks.put(generateTaskId(playerId), task);
            
            if (context.isDebugMode()) {
                String iterText = iterations == -1 ? "infinite" : String.valueOf(iterations);
                player.sendMessage("§7[DEBUG] Started async loop: " + iterText + " iterations, " + delayTicks + " tick delay");
            }
            
            return ExecutionResult.success("Async loop started");
        } catch (Exception e) {
            return ExecutionResult.error("Error starting async loop: " + e.getMessage());
        }
    }
    
    /**
     * Executes child blocks after a delay
     */
    private ExecutionResult executeDelayed(CodeBlock block, ExecutionContext context, ParameterResolver resolver, Player player) {
        try {
            // Resolve delay parameter
            DataValue delayValue = block.getParameter("delay");
            long delayTicks = delayValue != null ? 
                resolver.resolve(context, delayValue).asNumber().longValue() : 20L; // 1 second default
            
            // Safety validation
            delayTicks = Math.max(1, Math.min(delayTicks, 24000L)); // 1 tick to 20 minutes max
            
            // Check concurrent task limit
            UUID playerId = player.getUniqueId();
            if (countActiveTasks(playerId) >= MAX_CONCURRENT_TASKS) {
                player.sendMessage("§cToo many active async tasks! Maximum: " + MAX_CONCURRENT_TASKS);
                return ExecutionResult.error("Too many active async tasks");
            }
            
            // Schedule the delayed execution
            Bukkit.getScheduler().runTaskLater(context.getPlugin(), () -> {
                try {
                    // Execute child blocks
                    if (block.getChildren() != null) {
                        for (CodeBlock childBlock : block.getChildren()) {
                            executeChildBlock(context, childBlock);
                        }
                    }
                    
                    if (context.isDebugMode()) {
                        player.sendMessage("§7[DEBUG] Delayed execution completed");
                    }
                } catch (Exception e) {
                    player.sendMessage("§cError in delayed execution: " + e.getMessage());
                    if (context.isDebugMode()) {
                        context.getPlugin().getLogger().log(Level.SEVERE, "Stack trace: ", e);
                    }
                }
            }, delayTicks);
            
            if (context.isDebugMode()) {
                player.sendMessage("§7[DEBUG] Scheduled delayed execution: " + delayTicks + " ticks");
            }
            
            return ExecutionResult.success("Delayed execution scheduled");
        } catch (Exception e) {
            return ExecutionResult.error("Error scheduling delayed execution: " + e.getMessage());
        }
    }
    
    /**
     * Cancels an active async task
     */
    private ExecutionResult cancelAsyncTask(CodeBlock block, ExecutionContext context, ParameterResolver resolver, Player player) {
        try {
            // Resolve task ID parameter
            DataValue taskIdValue = block.getParameter("taskId");
            if (taskIdValue == null) {
                return ExecutionResult.error("Task ID parameter is required");
            }
            
            String taskId = resolver.resolve(context, taskIdValue).asString();
            
            // Try to cancel the task
            UUID taskUUID;
            try {
                taskUUID = UUID.fromString(taskId);
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid task ID format");
            }
            
            BukkitTask task = activeTasks.get(taskUUID);
            if (task != null) {
                task.cancel();
                activeTasks.remove(taskUUID);
                return ExecutionResult.success("Async task cancelled");
            } else {
                return ExecutionResult.error("Task not found or already completed");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error cancelling async task: " + e.getMessage());
        }
    }
    
    /**
     * Counts active tasks for a specific player
     */
    private int countActiveTasks(UUID playerId) {
        return (int) activeTasks.keySet().stream()
                              .filter(id -> id.toString().startsWith(playerId.toString()))
                              .count();
    }
    
    /**
     * Generates unique task ID for tracking
     */
    private UUID generateTaskId(UUID playerId) {
        return UUID.nameUUIDFromBytes((playerId.toString() + System.currentTimeMillis()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    
    /**
     * Cleanup method to stop all tasks for a player (called on disconnect)
     */
    public static void cleanupPlayerTasks(UUID playerId) {
        activeTasks.entrySet().removeIf(entry -> {
            if (entry.getKey().toString().startsWith(playerId.toString())) {
                entry.getValue().cancel();
                return true;
            }
            return false;
        });
    }
    
    /**
     * Emergency cleanup to stop all active tasks
     */
    public static void cleanupAllTasks() {
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();
    }
    
    /**
     * Executes a single child block using the appropriate action
     */
    private void executeChildBlock(ExecutionContext context, CodeBlock childBlock) {
        // This should integrate with the existing action registry system
        String action = childBlock.getAction();
        
        if (action != null) {
            // Get the action factory from the plugin's service registry
            com.megacreative.coding.ActionFactory actionFactory = context.getPlugin().getServiceRegistry().getService(com.megacreative.coding.ActionFactory.class);
            if (actionFactory != null) {
                // Create and execute the action
                com.megacreative.coding.BlockAction blockAction = actionFactory.createAction(action);
                if (blockAction != null) {
                    try {
                        blockAction.execute(childBlock, context);
                    } catch (Exception e) {
                        context.getPlugin().getLogger().severe("Error executing action " + action + " in async execution: " + e.getMessage());
                    }
                } else {
                    context.getPlugin().getLogger().warning("Unknown action in async execution: " + action);
                }
            }
            
            if (context.isDebugMode()) {
                Player player = context.getPlayer();
                if (player != null && action != null) {
                    player.sendMessage("§7[ASYNC] Executing: " + action);
                }
            }
        }
    }
    
    /**
     * Inner class handling the actual async loop execution
     */
    private static class AsyncLoopTask implements Runnable {
        private static final int ITERATION_LOG_INTERVAL = 100;
        
        private final ExecutionContext context;
        private final CodeBlock loopBlock;
        private final int maxIterations;
        private int currentIteration;
        
        public AsyncLoopTask(ExecutionContext context, CodeBlock loopBlock, int maxIterations) {
            this.context = context;
            this.loopBlock = loopBlock;
            this.maxIterations = maxIterations == -1 ? -1 : Math.max(1, maxIterations);
            this.currentIteration = 0;
        }
        
        @Override
        public void run() {
            try {
                if (!validateExecution()) {
                    return;
                }
                
                executeChildBlocks();
                currentIteration++;
                logProgress();
                
                // Check if we should stop the loop
                if (maxIterations != -1 && currentIteration >= maxIterations) {
                    cleanup();
                }
            } catch (Exception e) {
                handleExecutionError(e);
            }
        }
        
        private boolean validateExecution() {
            Player player = context.getPlayer();
            if (player == null || !player.isOnline()) {
                cleanup();
                return false;
            }
            
            if (maxIterations != -1 && currentIteration >= maxIterations) {
                cleanup();
                return false;
            }
            
            return true;
        }
        
        private void executeChildBlocks() {
            if (loopBlock.getChildren() != null) {
                for (CodeBlock childBlock : loopBlock.getChildren()) {
                    try {
                        // Execute the child block using action factory
                        String action = childBlock.getAction();
                        
                        if (action != null) {
                            // Get the action factory from the plugin's service registry
                            com.megacreative.coding.ActionFactory actionFactory = context.getPlugin().getServiceRegistry().getService(com.megacreative.coding.ActionFactory.class);
                            if (actionFactory != null) {
                                // Create and execute the action
                                com.megacreative.coding.BlockAction blockAction = actionFactory.createAction(action);
                                if (blockAction != null) {
                                    try {
                                        blockAction.execute(childBlock, context);
                                    } catch (Exception e) {
                                        context.getPlugin().getLogger().severe("Error executing action " + action + " in async loop: " + e.getMessage());
                                    }
                                } else {
                                    context.getPlugin().getLogger().warning("Unknown action in async loop: " + action);
                                }
                            }
                        }
                    } catch (Exception e) {
                        context.getPlugin().getLogger().log(Level.WARNING, "Error executing child block in async loop", e);
                    }
                }
            }
        }
        
        private void logProgress() {
            if (context.isDebugMode() && currentIteration % ITERATION_LOG_INTERVAL == 0) {
                Player player = context.getPlayer();
                if (player != null && player.isOnline()) {
                    String iterText = maxIterations == -1 ? "infinite" : maxIterations + "";
                    player.sendMessage("§7[DEBUG] Async loop iteration " + currentIteration + "/" + iterText);
                }
            }
        }
        
        private void handleExecutionError(Exception e) {
            Player player = context.getPlayer();
            if (player != null && player.isOnline()) {
                player.sendMessage("§cError in async loop: " + e.getMessage());
            }
            context.getPlugin().getLogger().log(Level.SEVERE, "Error in async loop execution", e);
            cleanup();
        }
        
        private void cleanup() {
            // Remove from active tasks tracking
            activeTasks.values().removeIf(task -> task.getTaskId() == Bukkit.getScheduler().hashCode());
        }
    }
}