package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.executors.AdvancedExecutionEngine;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * 🎆 Enhanced Script Engine Interface with FrameLand-Style Execution Modes
 * 
 * Extends the basic ScriptEngine with advanced execution capabilities:
 * - Multiple execution modes (sync, async, delayed, batch, prioritized)
 * - Priority-based execution scheduling
 * - Performance monitoring and analytics
 * - Execution context isolation
 * - Resource management and throttling
 */
public interface EnhancedScriptEngine extends ScriptEngine {
    
    /**
     * Execute a script with specified execution mode and priority
     * 
     * @param script The script to execute
     * @param player The player context
     * @param mode The execution mode
     * @param priority The execution priority
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     */
    CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, 
                                                   AdvancedExecutionEngine.ExecutionMode mode, 
                                                   AdvancedExecutionEngine.Priority priority, 
                                                   String trigger);
    
    /**
     * Execute a code block with specified execution mode and priority
     * 
     * @param block The block to execute
     * @param player The player context
     * @param mode The execution mode
     * @param priority The execution priority
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     */
    CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, 
                                                  AdvancedExecutionEngine.ExecutionMode mode, 
                                                  AdvancedExecutionEngine.Priority priority, 
                                                  String trigger);
    
    /**
     * Execute a script with delayed execution
     * 
     * @param script The script to execute
     * @param player The player context
     * @param delayTicks The delay in server ticks
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     */
    CompletableFuture<ExecutionResult> executeScriptDelayed(CodeScript script, Player player, 
                                                           long delayTicks, String trigger);
    
    /**
     * Execute multiple scripts in batch mode
     * 
     * @param scripts Array of scripts to execute
     * @param player The player context
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with batch execution result
     */
    CompletableFuture<ExecutionResult[]> executeScriptsBatch(CodeScript[] scripts, Player player, String trigger);
    
    /**
     * Cancel all executions for a specific player
     * 
     * @param player The player whose executions to cancel
     */
    void cancelPlayerExecutions(Player player);
    
    /**
     * Get execution statistics and performance metrics
     * 
     * @return Execution statistics
     */
    AdvancedExecutionEngine.ExecutionStatistics getExecutionStatistics();
    
    /**
     * Set the maximum execution time for scripts
     * 
     * @param maxTimeMs Maximum execution time in milliseconds
     */
    void setMaxExecutionTime(long maxTimeMs);
    
    /**
     * Set the maximum instructions per tick to prevent lag
     * 
     * @param maxInstructions Maximum instructions per server tick
     */
    void setMaxInstructionsPerTick(int maxInstructions);
    
    /**
     * Check if the engine is currently overloaded
     * 
     * @return true if the engine is under heavy load
     */
    boolean isOverloaded();
    
    /**
     * Get the current throughput (executions per second)
     * 
     * @return Current execution throughput
     */
    double getCurrentThroughput();
    
    /**
     * Prioritize execution for critical operations
     * 
     * @param script The script to prioritize
     * @param player The player context
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     */
    default CompletableFuture<ExecutionResult> executeScriptCritical(CodeScript script, Player player, String trigger) {
        return executeScript(script, player, AdvancedExecutionEngine.ExecutionMode.SYNCHRONOUS, 
                           AdvancedExecutionEngine.Priority.CRITICAL, trigger);
    }
    
    /**
     * Execute script asynchronously for non-critical operations
     * 
     * @param script The script to execute
     * @param player The player context
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     */
    default CompletableFuture<ExecutionResult> executeScriptAsync(CodeScript script, Player player, String trigger) {
        return executeScript(script, player, AdvancedExecutionEngine.ExecutionMode.ASYNCHRONOUS, 
                           AdvancedExecutionEngine.Priority.NORMAL, trigger);
    }
    
    /**
     * Execute script with low priority for background operations
     * 
     * @param script The script to execute
     * @param player The player context
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     */
    default CompletableFuture<ExecutionResult> executeScriptBackground(CodeScript script, Player player, String trigger) {
        return executeScript(script, player, AdvancedExecutionEngine.ExecutionMode.ASYNCHRONOUS, 
                           AdvancedExecutionEngine.Priority.LOW, trigger);
    }
}