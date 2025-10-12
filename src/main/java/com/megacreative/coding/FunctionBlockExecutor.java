package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import java.util.logging.Logger;

/**
 * Executor for function blocks
 * This executor handles FUNCTION type blocks
 */
public class FunctionBlockExecutor implements BlockExecutor {
    private static final Logger LOGGER = java.util.logging.Logger.getLogger(FunctionBlockExecutor.class.getName());
    
    public FunctionBlockExecutor() {
        
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            LOGGER.warning("Attempted to execute null function block");
            return ExecutionResult.success("Block is null");
        }
        
        String functionAction = block.getAction();
        if (functionAction == null || functionAction.isEmpty()) {
            LOGGER.warning("Function block has null or empty action");
            return ExecutionResult.error("Function action is null or empty");
        }
        
        LOGGER.info("Processing function action: " + functionAction + " for player: " + 
                   (context.getPlayer() != null ? context.getPlayer().getName() : "unknown"));
        
        if ("callFunction".equals(functionAction)) {
            DataValue functionNameValue = block.getParameter("functionName");
            if (functionNameValue != null && !functionNameValue.isEmpty()) {
                String functionName = functionNameValue.asString();
                LOGGER.fine("Calling function: " + functionName);
                
                return ExecutionResult.success("Function " + functionName + " called");
            } else {
                LOGGER.warning("Function call has no function name parameter");
                return ExecutionResult.error("Function name not specified");
            }
        }
        
        LOGGER.fine("Function block processed with action: " + functionAction);
        return ExecutionResult.success("Function block processed with action: " + functionAction);
    }
}