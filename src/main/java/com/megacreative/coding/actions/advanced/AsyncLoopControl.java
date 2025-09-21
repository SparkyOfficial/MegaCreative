package com.megacreative.coding.actions.advanced;

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
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);

        try {
            // Resolve loop parameters
            int iterations = (int) resolveNumberParameter(resolver, context, block, "iterations", 1);
            long delayTicks = (long) resolveNumberParameter(resolver, context, block, "delay", 20);
            
            // Safety validation
            iterations = validateIterations(iterations);
            delayTicks = Math.max(1, Math.min(1200, delayTicks)); // 1 tick to 1 minute max
            
            // Check concurrent loop limit
            UUID playerId = player.getUniqueId();
            if (countActiveLoops(playerId) >= MAX_CONCURRENT_LOOPS) {
                player.sendMessage("§cToo many active loops! Maximum: " + MAX_CONCURRENT_LOOPS);
                return;
            }
            
            // Create the async loop
            AsyncLoopTask loopTask = new AsyncLoopTask(context, block, iterations, delayTicks);
            BukkitTask task = loopTask.runTaskTimer(context.getPlugin(), delayTicks, delayTicks);
            
            // Track the loop for cleanup
            activeLoops.put(generateLoopId(playerId), task);
            
            // Debug feedback
            if (context.isDebugMode()) {
                String iterText = iterations == -1 ? "infinite" : String.valueOf(iterations);
                player.sendMessage("§7[DEBUG] Started async loop: " + iterText + 
                                 " iterations, " + delayTicks + " tick delay");
            }
            
        } catch (Exception e) {
            player.sendMessage("§cError starting async loop: " + e.getMessage());
            if (context.isDebugMode()) {
                context.getPlugin().getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            }
        }
    }
    
    /**
     * Validates iteration count with safety limits
     */
    private int validateIterations(int iterations) {
        if (iterations == -1) return -1; // Infinite loop allowed
        return Math.max(1, Math.min(MAX_ITERATIONS, iterations));
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
        private final ExecutionContext context;
        private final CodeBlock loopBlock;
        private final int maxIterations;
        private final long startTime;
        private int currentIteration;
        
        public AsyncLoopTask(ExecutionContext context, CodeBlock loopBlock, int maxIterations, long delay) {
            this.context = context;
            this.loopBlock = loopBlock;
            this.maxIterations = maxIterations;
            this.startTime = System.currentTimeMillis();
            this.currentIteration = 0;
        }
        
        @Override
        public void run() {
            try {
                if (!isExecutionValid()) {
                    return;
                }
                
                executeChildBlocks();
                currentIteration++;
                logProgress();
                
            } catch (Exception e) {
                handleExecutionError(e);
            }
        }
        
        /**
         * Validates if the loop execution should continue
         */
        private boolean isExecutionValid() {
            if (!isPlayerOnline()) {
                cleanup();
                return false;
            }
            
            if (isIterationLimitReached()) {
                logCompletion();
                cleanup();
                return false;
            }
            
            if (isTimeLimitExceeded()) {
                context.getPlayer().sendMessage("§cAsync loop timed out after 10 minutes");
                cleanup();
                return false;
            }
            
            return true;
        }
        
        /**
         * Checks if the player is still online
         */
        private boolean isPlayerOnline() {
            Player player = context.getPlayer();
            return player != null && player.isOnline();
        }
        
        /**
         * Checks if the iteration limit has been reached
         */
        private boolean isIterationLimitReached() {
            return maxIterations != -1 && currentIteration >= maxIterations;
        }
        
        /**
         * Checks if the execution time limit has been exceeded
         */
        private boolean isTimeLimitExceeded() {
            return System.currentTimeMillis() - startTime > 600000; // 10 minutes
        }
        
        /**
         * Logs loop completion message in debug mode
         */
        private void logCompletion() {
            if (context.isDebugMode()) {
                Player player = context.getPlayer();
                if (player != null) {
                    player.sendMessage("§7[DEBUG] Async loop completed: " + currentIteration + " iterations");
                }
            }
        }
        
        /**
         * Logs progress every 100 iterations in debug mode
         */
        private void logProgress() {
            if (currentIteration % 100 == 0 && context.isDebugMode()) {
                Player player = context.getPlayer();
                if (player != null) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    player.sendMessage("§7[DEBUG] Loop progress: " + currentIteration + 
                                     " iterations in " + (elapsed / 1000.0) + "s");
                }
            }
        }
        
        /**
         * Handles errors during loop execution
         */
        private void handleExecutionError(Exception e) {
            Player player = context.getPlayer();
            if (player != null) {
                player.sendMessage("§cError in async loop iteration " + currentIteration + ": " + e.getMessage());
                if (context.isDebugMode()) {
                    context.getPlugin().getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
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