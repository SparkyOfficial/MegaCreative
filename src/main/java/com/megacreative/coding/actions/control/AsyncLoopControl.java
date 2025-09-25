package com.megacreative.coding.actions.control;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Advanced async loop control that executes child blocks repeatedly without blocking the main thread
 * Includes performance monitoring, loop limits, and proper cleanup to prevent memory leaks
 */
public class AsyncLoopControl implements BlockAction {
    
    // Static tracking of active loops to prevent runaway executions
    private static final Map<UUID, BukkitTask> activeLoops = new ConcurrentHashMap<>();
    private static final int MAX_CONCURRENT_LOOPS = 10; // Per player
    private static final int MAX_ITERATIONS = 1000; // Safety limit
    
    @Override
    public com.megacreative.coding.executors.ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return com.megacreative.coding.executors.ExecutionResult.error("Player or block is null");
        }

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) {
            return com.megacreative.coding.executors.ExecutionResult.error("Variable manager not available");
        }
        
        ParameterResolver resolver = new ParameterResolver(context);

        try {
            // Resolve loop parameters
            int iterations = (int) resolveNumberParameter(resolver, context, block, "iterations", 1);
            long delayTicks = (long) resolveNumberParameter(resolver, context, block, "delay", 20);
            
            // Safety validation with Math.clamp
            if (iterations != -1) { // -1 means infinite loop
                iterations = Math.max(1, Math.min(iterations, MAX_ITERATIONS));
            }
            delayTicks = Math.max(1, Math.min(delayTicks, 1200L)); // 1 tick to 1 minute max
            
            // Check concurrent loop limit
            UUID playerId = player.getUniqueId();
            if (countActiveLoops(playerId) >= MAX_CONCURRENT_LOOPS) {
                player.sendMessage("§cToo many active loops! Maximum: " + MAX_CONCURRENT_LOOPS);
                return com.megacreative.coding.executors.ExecutionResult.error("Too many active loops");
            }
            
            // Create the async loop
            AsyncLoopTask loopTask = new AsyncLoopTask(context, block, iterations);
            BukkitTask task = loopTask.runTaskTimer(context.getPlugin(), delayTicks, delayTicks);
            
            // Track the loop for cleanup
            activeLoops.put(generateLoopId(playerId), task);
            
            // Debug feedback
            if (context.isDebugMode()) {
                String iterText = iterations == -1 ? "infinite" : String.valueOf(iterations);
                player.sendMessage("§7[DEBUG] Started async loop: " + iterText + 
                                 " iterations, " + delayTicks + " tick delay");
            }
            
            return com.megacreative.coding.executors.ExecutionResult.success("Async loop started");
        } catch (Exception e) {
            player.sendMessage("§cError starting async loop: " + e.getMessage());
            if (context.isDebugMode()) {
                context.getPlugin().getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            }
            return com.megacreative.coding.executors.ExecutionResult.error("Error starting async loop: " + e.getMessage());
        }
    }
    
    
    /**
     * Counts active loops for a specific player
     */
    private int countActiveLoops(UUID playerId) {
        return (int) activeLoops.keySet().stream()
                              .filter(id -> id.toString().startsWith(playerId.toString()))
                              .count();
    }
    
    /**
     * Generates unique loop ID for tracking
     */
    private UUID generateLoopId(UUID playerId) {
        return UUID.nameUUIDFromBytes((playerId.toString() + System.currentTimeMillis()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    
    /**
     * Resolves a numeric parameter with fallback default
     */
    private double resolveNumberParameter(ParameterResolver resolver, ExecutionContext context, 
                                        CodeBlock block, String paramName, double defaultValue) {
        DataValue rawValue = block.getParameter(paramName);
        if (rawValue == null) return defaultValue;
        
        try {
            return resolver.resolve(context, rawValue).asNumber().doubleValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Cleanup method to stop all loops for a player (called on disconnect)
     */
    public static void cleanupPlayerLoops(UUID playerId) {
        activeLoops.entrySet().removeIf(entry -> {
            if (entry.getKey().toString().startsWith(playerId.toString())) {
                entry.getValue().cancel();
                return true;
            }
            return false;
        });
    }
    
    /**
     * Emergency cleanup to stop all active loops
     */
    public static void cleanupAllLoops() {
        activeLoops.values().forEach(BukkitTask::cancel);
        activeLoops.clear();
    }
    
    /**
     * Inner class handling the actual async loop execution
     */
    private static class AsyncLoopTask extends BukkitRunnable {
        private static final long MAX_EXECUTION_TIME_MS = 600000; // 10 minutes
        private static final int ITERATION_LOG_INTERVAL = 100;
        
        private final ExecutionContext context;
        private final CodeBlock loopBlock;
        private final int maxIterations;
        private final long startTime;
        private int currentIteration;
        
        public AsyncLoopTask(ExecutionContext context, CodeBlock loopBlock, int maxIterations) {
            this.context = context;
            this.loopBlock = loopBlock;
            this.maxIterations = maxIterations == -1 ? -1 : Math.max(1, Math.min(maxIterations, MAX_ITERATIONS));
            this.startTime = System.currentTimeMillis();
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
                
            } catch (Exception e) {
                handleExecutionError(e);
            }
        }
        
        private boolean validateExecution() {
            Player player = context.getPlayer();
            if (!isPlayerValid(player)) {
                cleanup();
                return false;
            }
            
            if (isMaxIterationsReached(player)) {
                cleanup();
                return false;
            }
            
            if (isExecutionTimeExceeded(player)) {
                cleanup();
                return false;
            }
            
            return true;
        }
        
        private boolean isPlayerValid(Player player) {
            return player != null && player.isOnline();
        }
        
        private boolean isMaxIterationsReached(Player player) {
            if (maxIterations != -1 && currentIteration >= maxIterations) {
                if (context.isDebugMode()) {
                    player.sendMessage("§7[DEBUG] Async loop completed: " + currentIteration + " iterations");
                }
                return true;
            }
            return false;
        }
        
        private boolean isExecutionTimeExceeded(Player player) {
            if (System.currentTimeMillis() - startTime > MAX_EXECUTION_TIME_MS) {
                player.sendMessage("§cAsync loop timed out after 10 minutes");
                return true;
            }
            return false;
        }
        
        private void logProgress() {
            if (context.isDebugMode() && currentIteration % ITERATION_LOG_INTERVAL == 0) {
                Player player = context.getPlayer();
                if (player != null) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    player.sendMessage("§7[DEBUG] Loop progress: " + currentIteration + 
                                     " iterations in " + (elapsed / 1000.0) + "s");
                }
            }
        }
        
        private void handleExecutionError(Exception e) {
            Player player = context.getPlayer();
            if (player != null) {
                player.sendMessage("§cError in async loop iteration " + currentIteration + ": " + e.getMessage());
                if (context.isDebugMode()) {
                    context.getPlugin().getLogger().log(Level.SEVERE, 
                        "Error in async loop iteration " + currentIteration, e);
                }
            }
            cleanup();
        }
        
        /**
         * Executes all child blocks of the loop control block
         */
        private void executeChildBlocks() {
            if (loopBlock.getChildren() == null || loopBlock.getChildren().isEmpty()) {
                return;
            }
            
            // Execute each child block in sequence
            for (CodeBlock childBlock : loopBlock.getChildren()) {
                try {
                    // Create a new execution context for each iteration
                    ExecutionContext childContext = new ExecutionContext(
                        context.getPlugin(),
                        context.getPlayer(), 
                        context.getCreativeWorld(),
                        context.getEvent(),
                        context.getBlockLocation(),
                        childBlock
                    );
                    executeChildBlock(childContext, childBlock);
                } catch (Exception e) {
                    // Log error but continue with next block
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage("§cError in loop iteration: " + e.getMessage());
                    }
                    context.getPlugin().getLogger().log(Level.SEVERE, "Error in AsyncLoopControl", e);
                }
            }
        }
        
        /**
         * Executes a single child block using the appropriate action
         */
        private void executeChildBlock(ExecutionContext childContext, CodeBlock childBlock) {
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
                            blockAction.execute(childBlock, childContext);
                        } catch (Exception e) {
                            context.getPlugin().getLogger().severe("Error executing action " + action + " in async loop: " + e.getMessage());
                        }
                    } else {
                        context.getPlugin().getLogger().warning("Unknown action in async loop: " + action);
                    }
                }
                
                if (context.isDebugMode()) {
                    Player player = context.getPlayer();
                    if (player != null) {
                        player.sendMessage("§7[LOOP] Executing: " + action + " (iteration " + currentIteration + ")");
                    }
                }
            }
        }
        
        /**
         * Cleanup the loop and remove from tracking
         */
        private void cleanup() {
            cancel();
            // Remove from active loops tracking
            activeLoops.entrySet().removeIf(entry -> entry.getValue().equals(this));
        }
    }
}