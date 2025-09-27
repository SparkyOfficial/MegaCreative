package com.megacreative.coding.actions.control;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.Constants;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.HashMap;

/**
 * Async-related actions handler
 * Contains actions that work with asynchronous operations
 */
@BlockMeta(id = "asyncActions", displayName = "§aAsync Actions", type = BlockType.ACTION)
public class AsyncActions implements BlockAction {
    
    // Action handlers map for async actions
    private static final Map<String, BiConsumer<ExecutionContext, Map<String, DataValue>>> ACTION_HANDLERS = new HashMap<>();
    
    static {
        initializeActionHandlers();
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            String actionType = block.getAction();
            if (actionType == null) {
                return ExecutionResult.error(Constants.UNKNOWN_ACTION_TYPE + "null");
            }
            
            // Get the action handler
            BiConsumer<ExecutionContext, Map<String, DataValue>> handler = ACTION_HANDLERS.get(actionType);
            if (handler != null) {
                // Execute the handler
                handler.accept(context, block.getParameters());
                return ExecutionResult.success(Constants.ACTION_EXECUTED_SUCCESSFULLY + actionType);
            } else {
                return ExecutionResult.error(Constants.UNKNOWN_ACTION_TYPE + actionType);
            }
        } catch (Exception e) {
            return ExecutionResult.error(Constants.FAILED_TO_EXECUTE_ACTION + e.getMessage());
        }
    }
    
    /**
     * Initialize all async action handlers
     */
    private static void initializeActionHandlers() {
        // === ASYNC OPERATIONS ===
        ACTION_HANDLERS.put("delayedAction", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                int delay = params.get("delay").asNumber().intValue();
                String action = params.get("action").asString();
                
                // Schedule delayed execution
                context.getPlugin().getServer().getScheduler().runTaskLater(
                    context.getPlugin(), 
                    () -> {
                        // For now, we'll just send a message
                        // In a full implementation, this would execute the delayed action
                        context.getPlayer().sendMessage("§aDelayed action executed after " + delay + " seconds");
                    }, 
                    delay * 20L // Convert seconds to ticks
                );
            } catch (Exception e) {
                context.getPlayer().sendMessage("§cError scheduling delayed action: " + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("asyncLoop", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                int iterations = params.get("iterations").asNumber().intValue();
                int delay = params.get("delay").asNumber().intValue();
                
                // For now, we'll just send a message
                // In a full implementation, this would create an async loop
                context.getPlayer().sendMessage("§aAsync loop started with " + iterations + " iterations and " + delay + " tick delay");
                
                // Schedule async loop
                for (int i = 0; i < iterations; i++) {
                    final int iteration = i;
                    context.getPlugin().getServer().getScheduler().runTaskLater(
                        context.getPlugin(),
                        () -> {
                            context.getPlayer().sendMessage("§aAsync loop iteration: " + (iteration + 1));
                        },
                        (i + 1) * delay
                    );
                }
            } catch (Exception e) {
                context.getPlayer().sendMessage("§cError creating async loop: " + e.getMessage());
            }
        });
    }
}