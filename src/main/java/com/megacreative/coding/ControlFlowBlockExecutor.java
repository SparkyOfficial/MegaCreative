package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;

/**
 * Executor for control flow blocks
 * This executor handles CONTROL type blocks (if-else, while, for-each, etc.)
 */
public class ControlFlowBlockExecutor implements BlockExecutor {
    
    private final ActionFactory actionFactory;
    private final ConditionFactory conditionFactory;
    
    public ControlFlowBlockExecutor(ActionFactory actionFactory, ConditionFactory conditionFactory) {
        this.actionFactory = actionFactory;
        this.conditionFactory = conditionFactory;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            return ExecutionResult.success("Block is null");
        }
        
        String controlAction = block.getAction();
        if (controlAction == null) {
            return ExecutionResult.error("Control action is null");
        }
        
        // Handle different control flow actions
        switch (controlAction) {
            case "conditionalBranch":
                return handleConditionalBranch(block, context);
            case "else":
                // ELSE block - just continue to next block
                return ExecutionResult.success("ELSE block processed");
            case "whileLoop":
                return handleWhileLoop(block, context);
            case "forEach":
                return handleForEachLoop(block, context);
            case "break":
                ExecutionResult breakResult = ExecutionResult.success();
                breakResult.setTerminated(true);
                return breakResult;
            case "continue":
                ExecutionResult continueResult = ExecutionResult.success();
                continueResult.setTerminated(true);
                return continueResult;
            default:
                return ExecutionResult.error("Unknown control action: " + controlAction);
        }
    }
    
    private ExecutionResult handleConditionalBranch(CodeBlock block, ExecutionContext context) {
        // Handle conditional branch logic
        return ExecutionResult.success("Conditional branch processed");
    }
    
    private ExecutionResult handleWhileLoop(CodeBlock block, ExecutionContext context) {
        // Handle while loop logic
        return ExecutionResult.success("While loop processed");
    }
    
    private ExecutionResult handleForEachLoop(CodeBlock block, ExecutionContext context) {
        // Handle for-each loop logic
        return ExecutionResult.success("For-each loop processed");
    }
}