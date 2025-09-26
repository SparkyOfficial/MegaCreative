package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;

/**
 * Executor for event blocks
 * This executor handles EVENT type blocks
 */
public class EventBlockExecutor implements BlockExecutor {
    
    public EventBlockExecutor() {
        // No dependencies needed for event blocks
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            return ExecutionResult.success("Block is null");
        }
        
        // Event blocks are typically root blocks that trigger other blocks
        // They don't have direct execution logic
        return ExecutionResult.success("Event block processed");
    }
}