package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.Constants;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta; // Added import
import com.megacreative.coding.BlockType; // Added import
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;

@BlockMeta(id = "while", displayName = "§aWhile", type = BlockType.ACTION) // Added annotation
public class WhileAction implements BlockAction {
    
    // Constants for magic numbers
    private static final int DEFAULT_MAX_ITERATIONS = 100;
    private static final int MIN_MAX_ITERATIONS = 1;
    private static final int MAX_MAX_ITERATIONS = 10000;
    private static final String WHILE_LOOP_CONTEXT = "while_loop";
    
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        // Validate inputs
        if (!validateInputs(block, context)) {
            return ExecutionResult.error("Player or block is null");
        }
        
        Player player = context.getPlayer();

        // Get parameters from the container configuration
        WhileParams params = getWhileParamsFromContainer(block, context);
        
        // Validate max iterations
        ExecutionResult validationResult = validateMaxIterations(params.maxIterations);
        if (validationResult != null) {
            return validationResult;
        }
        
        // Validate next block
        CodeBlock nextBlock = block.getNextBlock();
        if (nextBlock == null) {
            return ExecutionResult.error("No block to execute in while loop");
        }
        
        // Get ScriptEngine from ServiceRegistry
        ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
        if (scriptEngine == null) {
            return ExecutionResult.error("Failed to get ScriptEngine from service registry");
        }
        
        // Execute the while loop asynchronously
        executeWhileLoop(params, nextBlock, context, scriptEngine, player);
        
        return ExecutionResult.success("While loop started");
    }
    
    /**
     * Validates the inputs for the while action
     */
    private boolean validateInputs(CodeBlock block, ExecutionContext context) {
        return context.getPlayer() != null && block != null;
    }
    
    /**
     * Validates the max iterations parameter
     */
    private ExecutionResult validateMaxIterations(int maxIterations) {
        if (maxIterations < MIN_MAX_ITERATIONS) {
            return ExecutionResult.error("Max iterations must be greater than 0");
        }
        
        if (maxIterations > MAX_MAX_ITERATIONS) {
            return ExecutionResult.error("Maximum iterations is 10000");
        }
        
        return null; // Valid count
    }
    
    /**
     * Executes the while loop asynchronously
     */
    private void executeWhileLoop(WhileParams params, CodeBlock nextBlock, ExecutionContext context, 
                                 ScriptEngine scriptEngine, Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                int iterations = 0;
                while (iterations < params.maxIterations && evaluateCondition(params.condition, context)) {
                    if (!executeIterationWithControl(params, nextBlock, context, scriptEngine, player, iterations)) {
                        break;
                    }
                    iterations++;
                }
                
                player.sendMessage("While loop executed " + iterations + " iterations");
            } catch (Exception e) {
                logError(context, "Error executing while loop: " + e.getMessage(), e);
                player.sendMessage("Error executing while loop: " + e.getMessage());
            }
        });
    }
    
    /**
     * Evaluates the condition for the while loop
     */
    private boolean evaluateCondition(String condition, ExecutionContext context) {
        // This is a simplified condition evaluation
        // In a real implementation, you would parse and evaluate the condition properly
        if (condition == null || condition.isEmpty()) {
            return false;
        }
        
        // For now, we'll just check if it's "true" or "false"
        // A real implementation would parse expressions like "variable > 5"
        return "true".equalsIgnoreCase(condition.trim());
    }
    
    /**
     * Executes a single iteration with break/continue control
     */
    private boolean executeIterationWithControl(WhileParams params, CodeBlock nextBlock, ExecutionContext context, 
                                              ScriptEngine scriptEngine, Player player, int currentIndex) {
        // Check if we should break or continue before executing iteration
        if (shouldBreak(context, player, currentIndex + 1)) {
            return false;
        }
        
        if (shouldContinue(context, player, currentIndex + 1)) {
            return true; // Continue to next iteration
        }
        
        // Create a new context for each iteration
        ExecutionContext loopContext = createLoopContext(context, nextBlock, currentIndex, params.maxIterations);
        
        // Execute the block chain for this iteration
        if (!executeIteration(scriptEngine, nextBlock, player, loopContext, currentIndex)) {
            return false;
        }
        
        // Check if we should break or continue after executing iteration
        if (shouldBreak(context, player, currentIndex + 1)) {
            return false;
        }
        
        if (shouldContinue(context, player, currentIndex + 1)) {
            return true; // Continue to next iteration
        }
        
        return true; // Continue normally
    }
    
    /**
     * Checks if the loop should break
     */
    private boolean shouldBreak(ExecutionContext context, Player player, int iteration) {
        if (context.hasBreakFlag()) {
            context.clearBreakFlag();
            player.sendMessage("While loop terminated by break at iteration " + iteration);
            return true;
        }
        return false;
    }
    
    /**
     * Checks if the loop should continue
     */
    private boolean shouldContinue(ExecutionContext context, Player player, int iteration) {
        if (context.hasContinueFlag()) {
            context.clearContinueFlag();
            player.sendMessage("Skipping iteration " + iteration + " due to continue");
            return true;
        }
        return false;
    }
    
    /**
     * Creates a new execution context for a loop iteration
     */
    private ExecutionContext createLoopContext(ExecutionContext context, CodeBlock nextBlock, 
                                             int currentIndex, int maxIterations) {
        ExecutionContext loopContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
        loopContext.setVariable("loopIndex", currentIndex + 1);
        loopContext.setVariable("maxIterations", maxIterations);
        return loopContext;
    }
    
    /**
     * Executes a single iteration of the loop
     */
    private boolean executeIteration(ScriptEngine scriptEngine, CodeBlock nextBlock, 
                                   Player player, ExecutionContext loopContext, int currentIndex) {
        try {
            ExecutionResult result = scriptEngine.executeBlockChain(nextBlock, player, WHILE_LOOP_CONTEXT)
                .exceptionally(throwable -> {
                    logError(loopContext, "Error in iteration " + (currentIndex + 1) + ": " + throwable.getMessage(), throwable);
                    player.sendMessage("Error in iteration " + (currentIndex + 1) + ": " + throwable.getMessage());
                    return null;
                })
                .join(); // Wait for iteration to complete
            return true;
        } catch (Exception e) {
            logError(loopContext, "Error in iteration " + (currentIndex + 1) + ": " + e.getMessage(), e);
            player.sendMessage("Error in iteration " + (currentIndex + 1) + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets parameters from the container configuration
     */
    private WhileParams getWhileParamsFromContainer(CodeBlock block, ExecutionContext context) {
        WhileParams params = new WhileParams();
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get condition from the condition slot
                Integer conditionSlot = slotResolver.apply("condition_slot");
                if (conditionSlot != null) {
                    ItemStack conditionItem = block.getConfigItem(conditionSlot);
                    if (conditionItem != null && conditionItem.hasItemMeta()) {
                        // Extract condition from item
                        params.condition = getConditionFromItem(conditionItem);
                    }
                }
                
                // Get max iterations from the max iterations slot
                Integer maxIterationsSlot = slotResolver.apply("max_iterations_slot");
                if (maxIterationsSlot != null) {
                    ItemStack maxIterationsItem = block.getConfigItem(maxIterationsSlot);
                    if (maxIterationsItem != null && maxIterationsItem.hasItemMeta()) {
                        // Extract max iterations from item
                        params.maxIterations = getMaxIterationsFromItem(maxIterationsItem);
                    }
                }
            }
        } catch (Exception e) {
            logError(context, "Error getting parameters from container in WhileAction: " + e.getMessage(), e);
        }
        
        // Set defaults if not found
        if (params.condition == null) {
            params.condition = "true"; // Default condition
        }
        
        if (params.maxIterations <= 0) {
            params.maxIterations = DEFAULT_MAX_ITERATIONS; // Default max iterations
        }
        
        return params;
    }
    
    /**
     * Extracts condition from an item
     */
    private String getConditionFromItem(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Return the display name as the condition
                    return displayName;
                }
            }
            return "true"; // Default condition
        } catch (Exception e) {
            return "true"; // Default condition
        }
    }
    
    /**
     * Extracts max iterations from an item
     */
    private int getMaxIterationsFromItem(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse max iterations from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Math.max(MIN_MAX_ITERATIONS, Integer.parseInt(cleanName));
                }
            }
            
            // Fallback to item amount
            return Math.max(MIN_MAX_ITERATIONS, item.getAmount());
        } catch (Exception e) {
            return DEFAULT_MAX_ITERATIONS; // Default max iterations
        }
    }
    
    /**
     * Logs an error with the plugin's logger
     */
    private void logError(ExecutionContext context, String message, Throwable throwable) {
        if (context != null && context.getPlugin() != null) {
            context.getPlugin().getLogger().log(Level.WARNING, message, throwable);
        }
    }
    
    /**
     * Helper class to hold while loop parameters
     */
    private static class WhileParams {
        String condition = "true";
        int maxIterations = DEFAULT_MAX_ITERATIONS;
    }
}