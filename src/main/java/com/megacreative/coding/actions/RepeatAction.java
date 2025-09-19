package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.Constants;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class RepeatAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        // Validate inputs
        if (!validateInputs(block, context)) {
            return ExecutionResult.error(Constants.PLAYER_OR_BLOCK_IS_NULL);
        }
        
        Player player = context.getPlayer();

        // Get times parameter from the container configuration
        int times = getTimesFromContainer(block, context);
        
        // Validate repeat count
        ExecutionResult validationResult = validateRepeatCount(times);
        if (validationResult != null) {
            return validationResult;
        }
        
        // Validate next block
        CodeBlock nextBlock = block.getNextBlock();
        if (nextBlock == null) {
            return ExecutionResult.error(Constants.NO_BLOCK_TO_REPEAT);
        }
        
        // Get ScriptEngine from ServiceRegistry
        ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
        if (scriptEngine == null) {
            return ExecutionResult.error(Constants.FAILED_TO_GET_SCRIPT_ENGINE);
        }
        
        // Execute the repeat loop asynchronously
        executeRepeatLoop(times, nextBlock, context, scriptEngine, player);
        
        return ExecutionResult.success(Constants.REPEAT_ACTION_STARTED);
    }
    
    /**
     * Validates the inputs for the repeat action
     */
    private boolean validateInputs(CodeBlock block, ExecutionContext context) {
        return context.getPlayer() != null && block != null;
    }
    
    /**
     * Validates the repeat count parameter
     */
    private ExecutionResult validateRepeatCount(int times) {
        if (times <= 0) {
            return ExecutionResult.error(Constants.REPEAT_COUNT_MUST_BE_GREATER_THAN_0);
        }
        
        if (times > 1000) {
            return ExecutionResult.error(Constants.MAXIMUM_REPEAT_COUNT_IS_1000);
        }
        
        return null; // Valid count
    }
    
    /**
     * Executes the repeat loop asynchronously
     */
    private void executeRepeatLoop(int times, CodeBlock nextBlock, ExecutionContext context, 
                                  ScriptEngine scriptEngine, Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < times; i++) {
                    if (!executeIterationWithControl(times, nextBlock, context, scriptEngine, player, i)) {
                        break;
                    }
                }
                
                player.sendMessage(String.format(Constants.CYCLE_EXECUTED_N_TIMES, times));
            } catch (Exception e) {
                player.sendMessage(String.format(Constants.ERROR_EXECUTING_CYCLE, e.getMessage()));
            }
        });
    }
    
    /**
     * Executes a single iteration with break/continue control
     */
    private boolean executeIterationWithControl(int times, CodeBlock nextBlock, ExecutionContext context, 
                                              ScriptEngine scriptEngine, Player player, int currentIndex) {
        // Check if we should break or continue before executing iteration
        if (shouldBreak(context, player, currentIndex + 1)) {
            return false;
        }
        
        if (shouldContinue(context, player, currentIndex + 1)) {
            return true; // Continue to next iteration
        }
        
        // Create a new context for each iteration
        ExecutionContext loopContext = createLoopContext(context, nextBlock, currentIndex, times);
        
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
            player.sendMessage(String.format(Constants.REPEAT_LOOP_TERMINATED_BY_BREAK, iteration));
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
            player.sendMessage(String.format(Constants.SKIPPING_ITERATION_DUE_TO_CONTINUE, iteration));
            return true;
        }
        return false;
    }
    
    /**
     * Creates a new execution context for a loop iteration
     */
    private ExecutionContext createLoopContext(ExecutionContext context, CodeBlock nextBlock, 
                                             int currentIndex, int times) {
        ExecutionContext loopContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
        loopContext.setVariable("loopIndex", currentIndex + 1);
        loopContext.setVariable("loopCount", times);
        return loopContext;
    }
    
    /**
     * Executes a single iteration of the loop
     */
    private boolean executeIteration(ScriptEngine scriptEngine, CodeBlock nextBlock, 
                                   Player player, ExecutionContext loopContext, int currentIndex) {
        try {
            ExecutionResult result = scriptEngine.executeBlockChain(nextBlock, player, "repeat_loop")
                .exceptionally(throwable -> {
                    player.sendMessage(String.format(Constants.ERROR_IN_ITERATION, currentIndex + 1, throwable.getMessage()));
                    return null;
                })
                .join(); // Ждем завершения итерации
            return true;
        } catch (Exception e) {
            player.sendMessage(String.format(Constants.ERROR_IN_ITERATION, currentIndex + 1, e.getMessage()));
            return false;
        }
    }
    
    /**
     * Gets times parameter from the container configuration
     */
    private int getTimesFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get times from the times slot
                Integer timesSlot = slotResolver.apply("times_slot");
                if (timesSlot != null) {
                    ItemStack timesItem = block.getConfigItem(timesSlot);
                    if (timesItem != null && timesItem.hasItemMeta()) {
                        // Extract times from item
                        return getTimesFromItem(timesItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting times parameter from container in RepeatAction: " + e.getMessage());
        }
        
        return 1; // Default to 1 iteration
    }
    
    /**
     * Extracts times from an item
     */
    private int getTimesFromItem(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse times from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Math.max(1, Integer.parseInt(cleanName));
                }
            }
            
            // Fallback to item amount
            return Math.max(1, item.getAmount());
        } catch (Exception e) {
            return 1; // Default to 1 iteration
        }
    }
}