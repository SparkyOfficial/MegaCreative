package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;
import java.util.logging.Logger;

/**
 * Executor for action blocks
 * This executor handles ACTION type blocks by delegating to the ActionFactory
 * 
 * Исполнитель для блоков действий
 * Этот исполнитель обрабатывает блоки типа ACTION, делегируя их ActionFactory
 * 
 * @author Андрій Budильников
 */
public class ActionBlockExecutor implements BlockExecutor {
    private static final Logger LOGGER = java.util.logging.Logger.getLogger(ActionBlockExecutor.class.getName());
    
    private final ActionFactory actionFactory;
    
    public ActionBlockExecutor(ActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            LOGGER.warning("Attempted to execute null block");
            return ExecutionResult.success("Block is null");
        }
        
        String actionId = block.getAction();
        if (actionId == null || actionId.isEmpty()) {
            LOGGER.warning("Block has null or empty action ID");
            return ExecutionResult.error("Action ID is null or empty");
        }
        
        
        LOGGER.info("Executing action: " + actionId + " for player: " + 
                   (context.getPlayer() != null ? context.getPlayer().getName() : "unknown"));
        
        BlockAction actionHandler = actionFactory.createAction(actionId);
        if (actionHandler != null) {
            try {
                ExecutionResult result = actionHandler.execute(block, context);
                if (result != null) {
                    if (result.isSuccess()) {
                        LOGGER.fine("Action " + actionId + " executed successfully: " + result.getMessage());
                    } else {
                        LOGGER.warning("Action " + actionId + " failed: " + result.getMessage());
                    }
                    return result;
                } else {
                    LOGGER.warning("Action " + actionId + " returned null result");
                    return ExecutionResult.error("Action returned null result");
                }
            } catch (Exception e) {
                LOGGER.severe("Exception during execution of action " + actionId + ": " + e.getMessage());
                LOGGER.severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
                return ExecutionResult.error("Exception during action execution: " + e.getMessage());
            }
        } else {
            LOGGER.warning("No action handler found for: " + actionId);
            return ExecutionResult.error("Action handler not found for: " + actionId);
        }
    }
}