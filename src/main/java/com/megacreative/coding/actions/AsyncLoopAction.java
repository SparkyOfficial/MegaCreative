package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncLoopAction implements BlockAction {
    
    private static final Map<String, BukkitTask> activeLoops = new ConcurrentHashMap<>();

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue iterationsValue = block.getParameter("iterations", DataValue.of(-1)); // -1 for infinite
            DataValue delayValue = block.getParameter("delay", DataValue.of(20)); // Default 20 ticks (1 second)
            
            int iterations = iterationsValue.asNumber().intValue();
            int delay = delayValue.asNumber().intValue();
            
            if (block.getChildren().isEmpty()) {
                return ExecutionResult.error("Loop has no blocks inside to execute.");
            }
            
            // Generate a unique ID for this loop
            String loopId = context.getPlayer().getUniqueId().toString() + "_" + System.currentTimeMillis();
            
            CodeBlock firstChild = block.getChildren().get(0);
            
            BukkitTask task = new BukkitRunnable() {
                private int count = 0;
                
                @Override
                public void run() {
                    // Check if we should stop
                    if ((iterations != -1 && count >= iterations) || context.isCancelled()) {
                        activeLoops.remove(loopId);
                        this.cancel();
                        return;
                    }
                    
                    // Execute the child blocks
                    try {
                        context.getPlugin().getServer().getScheduler().runTask(context.getPlugin(), () -> {
                            // Process the child block chain using the script engine from service registry
                            ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
                            scriptEngine.executeBlockChain(firstChild, context.getPlayer(), "loop");
                        });
                    } catch (Exception e) {
                        context.getPlugin().getLogger().severe("Error in async loop: " + e.getMessage());
                        activeLoops.remove(loopId);
                        this.cancel();
                        return;
                    }
                    
                    count++;
                }
            }.runTaskTimerAsynchronously(context.getPlugin(), 0L, delay);
            
            // Store the task so it can be cancelled later if needed
            activeLoops.put(loopId, task);
            
            // For async loops, we don't block the main execution chain
            return ExecutionResult.success("Started async loop with ID: " + loopId);
        } catch (Exception e) {
            return ExecutionResult.error("Error starting async loop: " + e.getMessage());
        }
    }
    
    public static void cancelAllLoopsForPlayer(String playerId) {
        activeLoops.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(playerId)) {
                entry.getValue().cancel();
                return true;
            }
            return false;
        });
    }
}