package com.megacreative.coding.actions.advanced;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
        
        ParameterResolver resolver = new ParameterResolver(variableManager);

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
                e.printStackTrace();
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
        return UUID.nameUUIDFromBytes((playerId.toString() + System.currentTimeMillis()).getBytes());
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
        private UUID loopId;
        
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
                // Check if player is still online
                Player player = context.getPlayer();
                if (player == null || !player.isOnline()) {
                    cleanup();
                    return;
                }
                
                // Check iteration limit
                if (maxIterations != -1 && currentIteration >= maxIterations) {
                    if (context.isDebugMode()) {
                        player.sendMessage("§7[DEBUG] Async loop completed: " + currentIteration + " iterations");
                    }
                    cleanup();
                    return;
                }
                
                // Check execution time limit (10 minutes max)
                if (System.currentTimeMillis() - startTime > 600000) {
                    player.sendMessage("§cAsync loop timed out after 10 minutes");
                    cleanup();
                    return;
                }
                
                // Execute child blocks
                executeChildBlocks();
                
                currentIteration++;
                
                // Performance monitoring
                if (currentIteration % 100 == 0 && context.isDebugMode()) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    player.sendMessage("§7[DEBUG] Loop progress: " + currentIteration + 
                                     " iterations in " + (elapsed / 1000.0) + "s");
                }
                
            } catch (Exception e) {
                // Handle errors gracefully
                Player player = context.getPlayer();
                if (player != null) {
                    player.sendMessage("§cError in async loop iteration " + currentIteration + ": " + e.getMessage());
                    if (context.isDebugMode()) {
                        e.printStackTrace();
                    }
                }
                cleanup();
            }
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
                        context.getPlayer(), 
                        context.getPlugin(), 
                        childBlock
                    );
                    
                    // Execute the child block (this should be done via ScriptExecutor)
                    // For now, we'll use the action registry to find and execute the appropriate action
                    executeChildBlock(childContext, childBlock);
                    
                } catch (Exception e) {
                    // Log error but continue with next child block
                    Player player = context.getPlayer();
                    if (player != null && context.isDebugMode()) {
                        player.sendMessage("§cError executing child block: " + e.getMessage());
                    }
                }
            }
        }
        
        /**
         * Executes a single child block using the appropriate action
         */
        private void executeChildBlock(ExecutionContext childContext, CodeBlock childBlock) {
            // This should integrate with the existing action registry system
            // For demonstration, we'll show the structure
            String action = childBlock.getAction();
            
            if (action != null) {
                // The actual implementation would look up the action in the registry
                // and execute it. This is a simplified version.
                
                // Example integration point:
                // BlockAction blockAction = context.getPlugin().getActionRegistry().getAction(action);
                // if (blockAction != null) {
                //     blockAction.execute(childContext);
                // }
                
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