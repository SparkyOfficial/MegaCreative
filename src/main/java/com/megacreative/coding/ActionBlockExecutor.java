package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;

/**
 * Executor for action blocks
 * This executor handles ACTION type blocks by delegating to the ActionFactory
 */
public class ActionBlockExecutor implements BlockExecutor {
    
    private final ActionFactory actionFactory;
    
    public ActionBlockExecutor(ActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            return ExecutionResult.success("Block is null");
        }
        
        String actionId = block.getAction();
        if (actionId == null) {
            return ExecutionResult.error("Action ID is null");
        }
        
        BlockAction actionHandler = actionFactory.createAction(actionId);
        if (actionHandler != null) {
            return actionHandler.execute(block, context);
        } else {
            return ExecutionResult.error("Action handler not found for: " + actionId);
        }
    }
}