package com.megacreative.coding;

import com.megacreative.coding.BlockType;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

/**
 * Unified interface for script execution engines.
 * Provides a consistent API for executing scripts with support for both
 * synchronous and asynchronous execution modes.
 */
public interface ScriptEngine {

    /**
     * Executes a script asynchronously.
     *
     * @param script The script to execute
     * @param player The player who triggered the execution (can be null for console/automated)
     * @param trigger The event or condition that triggered execution
     * @return A CompletableFuture that completes when execution finishes
     */
    CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, String trigger);
    
    /**
     * Executes a single code block asynchronously.
     * This is used for executing individual blocks within a script.
     *
     * @param block The code block to execute
     * @param player The player who triggered the execution (can be null)
     * @param trigger The event or condition that triggered execution
     * @return A CompletableFuture that completes when the block execution finishes
     */
    CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, String trigger);
    
    /**
     * Executes a chain of code blocks starting from a specific block.
     * This is used for executing blocks that trigger other blocks (like WaitAction, RepeatAction).
     *
     * @param startBlock The first block in the chain to execute
     * @param player The player who triggered the execution (can be null)
     * @param trigger The event or condition that triggered execution
     * @return A CompletableFuture that completes when the block chain execution finishes
     */
    CompletableFuture<ExecutionResult> executeBlockChain(CodeBlock startBlock, Player player, String trigger);

    /**
     * Registers a block action handler.
     *
     * @param type The block type this action handles
     * @param action The action implementation
     */
    void registerAction(BlockType type, BlockAction action);

    /**
     * Registers a block condition handler.
     *
     * @param type The block type this condition handles
     * @param condition The condition implementation
     */
    void registerCondition(BlockType type, BlockCondition condition);
    
    /**
     * Gets the block type for a given material and action name.
     * This is used to map configuration-based block types to their enum representations.
     *
     * @param material The block material
     * @param actionName The action name from configuration
     * @return The corresponding BlockType, or null if not found
     */
    BlockType getBlockType(Material material, String actionName);

    /**
     * Gets the variable manager for this engine.
     * @return The VariableManager instance
     */
    VariableManager getVariableManager();

    /**
     * Gets the visual debugger for this engine.
     * @return The VisualDebugger instance
     */
    VisualDebugger getDebugger();

    /**
     * Pauses execution of a running script.
     *
     * @param executionId The ID of the execution to pause
     * @return true if the execution was found and paused
     */
    boolean pauseExecution(String executionId);

    /**
     * Resumes a paused script execution.
     *
     * @param executionId The ID of the execution to resume
     * @return true if the execution was found and resumed
     */
    boolean resumeExecution(String executionId);

    /**
     * Steps through a single block in a paused script.
     *
     * @param executionId The ID of the execution to step
     * @return true if the step was successful
     */
    boolean stepExecution(String executionId);

    /**
     * Stops a running script execution.
     *
     * @param executionId The ID of the execution to stop
     * @return true if the execution was found and stopped
     */
    boolean stopExecution(String executionId);
}