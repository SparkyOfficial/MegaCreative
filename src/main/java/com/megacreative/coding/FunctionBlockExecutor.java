package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;

/**
 * Executor for function blocks
 * This executor handles FUNCTION type blocks
 */
public class FunctionBlockExecutor implements BlockExecutor {
    
    public FunctionBlockExecutor() {
        // No dependencies needed for basic function blocks
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            return ExecutionResult.success("Block is null");
        }
        
        String functionAction = block.getAction();
        if ("callFunction".equals(functionAction)) {
            String functionName = block.getParameterValue("functionName", String.class);
            if (functionName != null) {
                // Function calling logic would go here
                return ExecutionResult.success("Function " + functionName + " called");
            } else {
                return ExecutionResult.error("Function name not specified");
            }
        }
        
        return ExecutionResult.success("Function block processed");
    }
}