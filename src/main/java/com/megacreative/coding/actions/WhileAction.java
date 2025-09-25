package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.ConditionFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;

public class WhileAction implements BlockAction {
    
    // Constants for magic numbers
    private static final int DEFAULT_MAX_ITERATIONS = 100;
    private static final int MIN_ITERATION_COUNT = 1;
    private static final int MAX_ITERATION_COUNT = 10000;
    private static final String WHILE_LOOP_CONTEXT = "while_loop";
    
    @Override
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
        if (maxIterations < MIN_ITERATION_COUNT) {
            return ExecutionResult.error("Max iterations must be greater than 0");
        }
        
        if (maxIterations > MAX_ITERATION_COUNT) {
            return ExecutionResult.error("Maximum iterations is " + MAX_ITERATION_COUNT);
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
                boolean conditionResult = evaluateCondition(params.condition, context);
                
                while (conditionResult && iterations < params.maxIterations) {
                    // Check if we should break or continue
                    if (shouldBreak(context, player, iterations + 1)) {
                        break;
                    }
                    
                    if (shouldContinue(context, player, iterations + 1)) {
                        iterations++;
                        conditionResult = evaluateCondition(params.condition, context);
                        continue; // Continue to next iteration
                    }
                    
                    // Execute the block chain for this iteration
                    if (!executeIteration(scriptEngine, nextBlock, player, context, iterations)) {
                        break;
                    }
                    
                    // Check if we should break or continue after executing iteration
                    if (shouldBreak(context, player, iterations + 1)) {
                        break;
                    }
                    
                    if (shouldContinue(context, player, iterations + 1)) {
                        iterations++;
                        conditionResult = evaluateCondition(params.condition, context);
                        continue; // Continue to next iteration
                    }
                    
                    iterations++;
                    conditionResult = evaluateCondition(params.condition, context);
                }
                
                player.sendMessage(String.format("§aWhile loop executed %d iterations", iterations));
            } catch (Exception e) {
                logError(context, "Error executing while loop: " + e.getMessage(), e);
                player.sendMessage(String.format("§cError executing while loop: %s", e.getMessage()));
            }
        });
    }
    
    /**
     * Evaluates the condition for the while loop
     */
    private boolean evaluateCondition(String conditionStr, ExecutionContext context) {
        if (conditionStr == null || conditionStr.isEmpty()) {
            return true; // Default to true if no condition specified
        }
        
        try {
            // Create a condition factory to evaluate the condition
            ConditionFactory conditionFactory = new ConditionFactory();
            
            // For simplicity, we'll assume the condition is a variable comparison
            // In a real implementation, this would be more complex
            if (conditionStr.contains("==")) {
                String[] parts = conditionStr.split("==");
                if (parts.length == 2) {
                    String varName = parts[0].trim();
                    String compareValue = parts[1].trim();
                    
                    // Get variable value
                    Object varValue = context.getVariable(varName);
                    if (varValue != null) {
                        return varValue.toString().equals(compareValue);
                    } else {
                        // If variable doesn't exist, compare with empty string
                        return "".equals(compareValue);
                    }
                }
            }
            
            // Default to true for unknown conditions
            return true;
        } catch (Exception e) {
            logError(context, "Error evaluating while condition: " + e.getMessage(), e);
            return false; // Default to false on error
        }
    }
    
    /**
     * Checks if the loop should break
     */
    private boolean shouldBreak(ExecutionContext context, Player player, int iteration) {
        if (context.hasBreakFlag()) {
            context.clearBreakFlag();
            player.sendMessage(String.format("§aWhile loop terminated by break at iteration %d", iteration));
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
            player.sendMessage(String.format("§aSkipping iteration %d due to continue", iteration));
            return true;
        }
        return false;
    }
    
    /**
     * Executes a single iteration of the loop
     */
    private boolean executeIteration(ScriptEngine scriptEngine, CodeBlock nextBlock, 
                                   Player player, ExecutionContext context, int currentIndex) {
        try {
            ExecutionResult result = scriptEngine.executeBlockChain(nextBlock, player, WHILE_LOOP_CONTEXT)
                .exceptionally(throwable -> {
                    logError(context, "Error in iteration " + (currentIndex + 1) + ": " + throwable.getMessage(), throwable);
                    player.sendMessage(String.format("§cError in iteration %d: %s", currentIndex + 1, throwable.getMessage()));
                    return null;
                })
                .join(); // Wait for completion
            return true;
        } catch (Exception e) {
            logError(context, "Error in iteration " + (currentIndex + 1) + ": " + e.getMessage(), e);
            player.sendMessage(String.format("§cError in iteration %d: %s", currentIndex + 1, e.getMessage()));
            return false;
        }
    }
    
    /**
     * Gets while parameters from the container configuration
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
                Integer conditionSlot = slotResolver.apply("condition");
                if (conditionSlot != null) {
                    ItemStack conditionItem = block.getConfigItem(conditionSlot);
                    if (conditionItem != null && conditionItem.hasItemMeta()) {
                        // Extract condition from item
                        params.condition = getConditionFromItem(conditionItem);
                    }
                }
                
                // Get max iterations from the max iterations slot
                Integer maxIterationsSlot = slotResolver.apply("max_iterations");
                if (maxIterationsSlot != null) {
                    ItemStack maxIterationsItem = block.getConfigItem(maxIterationsSlot);
                    if (maxIterationsItem != null && maxIterationsItem.hasItemMeta()) {
                        // Extract max iterations from item
                        params.maxIterations = getMaxIterationsFromItem(maxIterationsItem);
                    }
                }
            }
            
            // Fallback to parameter-based configuration
            DataValue conditionParam = block.getParameter("condition");
            if (conditionParam != null && !conditionParam.isEmpty()) {
                params.condition = conditionParam.asString();
            }
            
            DataValue maxIterationsParam = block.getParameter("maxIterations");
            if (maxIterationsParam != null && !maxIterationsParam.isEmpty()) {
                try {
                    params.maxIterations = Integer.parseInt(maxIterationsParam.asString());
                } catch (NumberFormatException e) {
                    // Use default
                }
            }
        } catch (Exception e) {
            logError(context, "Error getting while parameters from container: " + e.getMessage(), e);
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
                    // Remove color codes and return the condition
                    return displayName.replaceAll("[§0-9]", "").trim();
                }
            }
        } catch (Exception e) {
            // Return null on error
        }
        return null;
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
                    return Math.max(MIN_ITERATION_COUNT, Integer.parseInt(cleanName));
                }
            }
            
            // Fallback to item amount
            return Math.max(MIN_ITERATION_COUNT, item.getAmount());
        } catch (Exception e) {
            return DEFAULT_MAX_ITERATIONS; // Default to 100 iterations
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
     * Helper class to hold while parameters
     */
    private static class WhileParams {
        String condition = "";
        int maxIterations = DEFAULT_MAX_ITERATIONS;
    }
}