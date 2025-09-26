package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;

/**
 * Interface for executing code blocks
 * This is part of the Strategy pattern implementation for the ScriptEngine
 */
public interface BlockExecutor {
    
    /**
     * Execute a code block
     * @param block The code block to execute
     * @param context The execution context
     * @return The result of the execution
     */
    ExecutionResult execute(CodeBlock block, ExecutionContext context);
}