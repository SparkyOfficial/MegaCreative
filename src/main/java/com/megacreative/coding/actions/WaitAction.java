package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;

/**
 * Action to wait for a specified amount of time
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "wait", displayName = "§bWait", type = BlockType.ACTION)
public class WaitAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameter
            DataValue delayValue = block.getParameter("delay");
            
            if (delayValue == null) {
                return ExecutionResult.error("Missing required parameter: delay");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedDelay = resolver.resolve(context, delayValue);
            
            int delay = resolvedDelay.asNumber().intValue();
            
            // Wait (this is a simple implementation - in a real system you might want to handle this differently)
            try {
                Thread.sleep(delay * 50L); // Convert ticks to milliseconds (1 tick = 50ms)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ExecutionResult.error("Wait interrupted");
            }
            
            return ExecutionResult.success("Waited for " + delay + " ticks");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to wait: " + e.getMessage());
        }
    }
}