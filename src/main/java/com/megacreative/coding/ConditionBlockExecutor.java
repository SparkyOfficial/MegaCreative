package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;
import java.util.logging.Logger;

/**
 * Executor for condition blocks
 * This executor handles CONDITION type blocks by delegating to the ConditionFactory
 * 
 * Исполнитель для блоков условий
 * Этот исполнитель обрабатывает блоки типа CONDITION, делегируя их ConditionFactory
 * 
 * @author Андрій Budильников
 */
public class ConditionBlockExecutor implements BlockExecutor {
    private static final Logger LOGGER = java.util.logging.Logger.getLogger(ConditionBlockExecutor.class.getName());
    
    private final ConditionFactory conditionFactory;
    
    public ConditionBlockExecutor(ConditionFactory conditionFactory) {
        this.conditionFactory = conditionFactory;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            LOGGER.warning("Attempted to execute null condition block");
            return ExecutionResult.success("Block is null");
        }
        
        String conditionId = block.getAction();
        if (conditionId == null || conditionId.isEmpty()) {
            LOGGER.warning("Condition block has null or empty condition ID");
            return ExecutionResult.error("Condition ID is null or empty");
        }
        
        
        LOGGER.info("Evaluating condition: " + conditionId + " for player: " + 
                   (context.getPlayer() != null ? context.getPlayer().getName() : "unknown"));
        
        BlockCondition conditionHandler = conditionFactory.createCondition(conditionId);
        if (conditionHandler != null) {
            try {
                boolean result = conditionHandler.evaluate(block, context);
                LOGGER.fine("Condition " + conditionId + " evaluated to " + result);
                
                return new ExecutionResult.Builder()
                    .success(true)
                    .message("Condition " + conditionId + " evaluated to " + result)
                    .addDetail("condition_result", result)
                    .build();
            } catch (Exception e) {
                LOGGER.severe("Exception during evaluation of condition " + conditionId + ": " + e.getMessage());
                LOGGER.severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
                return ExecutionResult.error("Exception during condition evaluation: " + e.getMessage());
            }
        } else {
            LOGGER.warning("No condition handler found for: " + conditionId);
            return ExecutionResult.error("Condition handler not found for: " + conditionId);
        }
    }
}