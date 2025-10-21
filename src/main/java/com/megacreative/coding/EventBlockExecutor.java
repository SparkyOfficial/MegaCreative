package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;
import java.util.logging.Logger;

/**
 * Executor for event blocks
 * This executor handles EVENT type blocks
 * 
 * Исполнитель для блоков событий
 * Этот исполнитель обрабатывает блоки типа EVENT
 * 
 * @author Андрій Budильников
 */
public class EventBlockExecutor implements BlockExecutor {
    private static final Logger LOGGER = java.util.logging.Logger.getLogger(EventBlockExecutor.class.getName());
    
    public EventBlockExecutor() {
        
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            LOGGER.warning("Attempted to execute null event block");
            return ExecutionResult.success("Block is null");
        }
        
        String eventId = block.getEvent();
        if (eventId == null || eventId.isEmpty()) {
            LOGGER.warning("Event block has null or empty event ID");
            return ExecutionResult.error("Event ID is null or empty");
        }
        
        String actionId = block.getAction();
        LOGGER.info("Processing event block: " + eventId + " with action: " + 
                   (actionId != null ? actionId : "none") + " for player: " + 
                   (context.getPlayer() != null ? context.getPlayer().getName() : "unknown"));
        
        
        
        LOGGER.fine("Event block processed for event: " + eventId);
        return ExecutionResult.success("Event block processed for event: " + eventId);
    }
}