package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class AsyncLoopAction implements BlockAction {
    
    private static final Map<String, BukkitTask> activeLoops = new ConcurrentHashMap<>();

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters from the container configuration
            LoopParams params = getLoopParamsFromContainer(block, context);
            
            if (block.getChildren().isEmpty()) {
                return ExecutionResult.error("Loop has no blocks inside to execute.");
            }
            
            // Check if player is available
            if (context.getPlayer() == null) {
                return ExecutionResult.error("Player required for async loop");
            }
            
            // Generate a unique ID for this loop
            String loopId = context.getPlayer().getUniqueId().toString() + "_" + System.currentTimeMillis();
            
            CodeBlock firstChild = block.getChildren().get(0);
            
            BukkitTask task = new BukkitRunnable() {
                private int count = 0;
                
                @Override
                public void run() {
                    // Check if we should stop
                    if ((params.iterations != -1 && count >= params.iterations) || context.isCancelled()) {
                        activeLoops.remove(loopId);
                        this.cancel();
                        return;
                    }
                    
                    // Check for break flag
                    if (context.hasBreakFlag()) {
                        context.clearBreakFlag();
                        activeLoops.remove(loopId);
                        this.cancel();
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§aAsync loop terminated by break statement");
                        }
                        return;
                    }
                    
                    // Check for continue flag
                    if (context.hasContinueFlag()) {
                        context.clearContinueFlag();
                        // Skip this iteration
                        count++;
                        return;
                    }
                    
                    // Execute the child blocks
                    try {
                        context.getPlugin().getServer().getScheduler().runTask(context.getPlugin(), () -> {
                            // Process the child block chain using the script engine from service registry
                            ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
                            if (scriptEngine != null) {
                                scriptEngine.executeBlockChain(firstChild, context.getPlayer(), "loop")
                                    .thenAccept(result -> {
                                        // Check for break/continue flags after execution
                                        if (context.hasBreakFlag()) {
                                            context.clearBreakFlag();
                                            activeLoops.remove(loopId);
                                            this.cancel();
                                            if (context.getPlayer() != null) {
                                                context.getPlayer().sendMessage("§aAsync loop terminated by break statement");
                                            }
                                        } else if (context.hasContinueFlag()) {
                                            context.clearContinueFlag();
                                            // Continue to next iteration
                                        }
                                    });
                            }
                        });
                    } catch (Exception e) {
                        context.getPlugin().getLogger().severe("Error in async loop: " + e.getMessage());
                        activeLoops.remove(loopId);
                        this.cancel();
                        return;
                    }
                    
                    count++;
                }
            }.runTaskTimerAsynchronously(context.getPlugin(), 0L, params.delay);
            
            // Store the task so it can be cancelled later if needed
            activeLoops.put(loopId, task);
            
            // For async loops, we don't block the main execution chain
            return ExecutionResult.success("Started async loop with ID: " + loopId);
        } catch (Exception e) {
            return ExecutionResult.error("Error starting async loop: " + e.getMessage());
        }
    }
    
    /**
     * Gets loop parameters from the container configuration
     */
    private LoopParams getLoopParamsFromContainer(CodeBlock block, ExecutionContext context) {
        LoopParams params = new LoopParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get iterations from the iterations slot
                Integer iterationsSlot = slotResolver.apply("iterations");
                if (iterationsSlot != null) {
                    ItemStack iterationsItem = block.getConfigItem(iterationsSlot);
                    if (iterationsItem != null && iterationsItem.hasItemMeta()) {
                        // Extract iterations from item
                        params.iterations = getIterationsFromItem(iterationsItem, -1);
                    }
                }
                
                // Get delay from the delay slot
                Integer delaySlot = slotResolver.apply("delay");
                if (delaySlot != null) {
                    ItemStack delayItem = block.getConfigItem(delaySlot);
                    if (delayItem != null && delayItem.hasItemMeta()) {
                        // Extract delay from item
                        params.delay = getDelayFromItem(delayItem, 20);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting loop parameters from container in AsyncLoopAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts iterations from an item
     */
    private int getIterationsFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse iterations from display name (e.g., "iterations:10")
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    if (cleanName.contains(":")) {
                        String[] parts = cleanName.split(":");
                        if (parts.length > 1) {
                            return Integer.parseInt(parts[1].trim());
                        }
                    }
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Extracts delay from an item
     */
    private int getDelayFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse delay from display name (e.g., "delay:20")
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    if (cleanName.contains(":")) {
                        String[] parts = cleanName.split(":");
                        if (parts.length > 1) {
                            return Math.max(1, Integer.parseInt(parts[1].trim()));
                        }
                    }
                }
            }
            
            // Fallback to item amount
            return Math.max(1, item.getAmount());
        } catch (Exception e) {
            return Math.max(1, defaultValue);
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
    
    /**
     * Helper class to hold loop parameters
     */
    private static class LoopParams {
        int iterations = -1; // -1 for infinite
        int delay = 20; // Default 20 ticks (1 second)
    }
}