package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;

/**
 * Executor for condition blocks
 * This executor handles CONDITION type blocks by delegating to the ConditionFactory
 */
public class ConditionBlockExecutor implements BlockExecutor {
    
    private final ConditionFactory conditionFactory;
    
    public ConditionBlockExecutor(ConditionFactory conditionFactory) {
        this.conditionFactory = conditionFactory;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            return ExecutionResult.success("Block is null");
        }
        
        String conditionId = block.getAction();
        if (conditionId == null) {
            return ExecutionResult.error("Condition ID is null");
        }
        
        BlockCondition conditionHandler = conditionFactory.createCondition(conditionId);
        if (conditionHandler != null) {
            boolean result = conditionHandler.evaluate(block, context);
            return ExecutionResult.success("Condition " + conditionId + " evaluated to " + result);
        } else {
            return ExecutionResult.error("Condition handler not found for: " + conditionId);
        }
    }
}