package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.Constants;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Action for repeating a block execution a specified number of times.
 * This action retrieves a repeat count and executes the next block that many times.
 */
@BlockMeta(id = "repeat", displayName = "§aRepeat", type = BlockType.ACTION)
public class RepeatAction implements BlockAction {

    
    private static final int DEFAULT_ITERATION_COUNT = 1;
    private static final int MIN_ITERATION_COUNT = 1;
    private static final int MAX_ITERATION_COUNT = 1000;
    private static final String REPEAT_LOOP_CONTEXT = "repeat_loop";
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        
        if (!validateInputs(block, context)) {
            return ExecutionResult.error(Constants.PLAYER_OR_BLOCK_IS_NULL);
        }
        
        Player player = context.getPlayer();

        
        int times = getTimesFromParameter(block, context);
        
        
        ExecutionResult validationResult = validateRepeatCount(times);
        if (validationResult != null) {
            return validationResult;
        }
        
        
        CodeBlock nextBlock = block.getNextBlock();
        if (nextBlock == null) {
            return ExecutionResult.error(Constants.NO_BLOCK_TO_REPEAT);
        }
        
        
        ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
        if (scriptEngine == null) {
            return ExecutionResult.error(Constants.FAILED_TO_GET_SCRIPT_ENGINE);
        }
        
        
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
        if (times < MIN_ITERATION_COUNT) {
            return ExecutionResult.error(Constants.REPEAT_COUNT_MUST_BE_GREATER_THAN_0);
        }
        
        if (times > MAX_ITERATION_COUNT) {
            return ExecutionResult.error(Constants.MAXIMUM_REPEAT_COUNT_IS_1000);
        }
        
        return null; 
    }
    
    /**
     * Executes the repeat loop asynchronously
     */
    private void executeRepeatLoop(int times, CodeBlock nextBlock, ExecutionContext context, 
                                  ScriptEngine scriptEngine, Player player) {
        
        executeRepeatLoopSequentially(times, nextBlock, context, scriptEngine, player, 0);
    }
    
    /**
     * Executes repeat loop sequentially using ScriptEngine's async capabilities
     */
    private void executeRepeatLoopSequentially(int times, CodeBlock nextBlock, ExecutionContext context, 
                                             ScriptEngine scriptEngine, Player player, int currentIndex) {
        
        if (currentIndex >= times) {
            player.sendMessage("§a🔄 Повторено " + times + " раз");
            return;
        }
        
        
        if (shouldBreak(context, player, currentIndex + 1)) {
            player.sendMessage("§a🔄 Повторено " + currentIndex + " раз (прервано)");
            return;
        }
        
        
        if (shouldContinue(context, player, currentIndex + 1)) {
            
            executeRepeatLoopSequentially(times, nextBlock, context, scriptEngine, player, currentIndex + 1);
            return;
        }
        
        
        ExecutionContext loopContext = createLoopContext(context, nextBlock, currentIndex, times);
        
        
        scriptEngine.executeBlockChain(nextBlock, player, REPEAT_LOOP_CONTEXT)
            .exceptionally(throwable -> {
                logError(loopContext, "Error in iteration " + (currentIndex + 1) + ": " + throwable.getMessage(), throwable);
                player.sendMessage(String.format(Constants.ERROR_IN_ITERATION, currentIndex + 1, throwable.getMessage()));
                return ExecutionResult.error(throwable.getMessage());
            })
            .thenAccept(result -> {
                
                if (shouldBreak(context, player, currentIndex + 1)) {
                    player.sendMessage("§a🔄 Повторено " + (currentIndex + 1) + " раз (прервано)");
                    return;
                }
                
                
                executeRepeatLoopSequentially(times, nextBlock, context, scriptEngine, player, currentIndex + 1);
            });
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
     * Gets times parameter from the new parameter system
     */
    private int getTimesFromParameter(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue timesValue = block.getParameter("times");
            
            if (timesValue != null && !timesValue.isEmpty()) {
                return Math.max(MIN_ITERATION_COUNT, Integer.parseInt(timesValue.asString()));
            }
        } catch (Exception e) {
            logError(context, "Error getting times parameter in RepeatAction: " + e.getMessage(), e);
        }
        
        return DEFAULT_ITERATION_COUNT; 
    }
    
    /**
     * Logs an error with the plugin's logger
     */
    private void logError(ExecutionContext context, String message, Throwable throwable) {
        if (context != null && context.getPlugin() != null) {
            context.getPlugin().getLogger().log(Level.WARNING, message, throwable);
        }
    }
}