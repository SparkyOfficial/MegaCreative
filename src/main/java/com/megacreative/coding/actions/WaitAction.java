package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;

/**
 * Action for waiting/pausing execution for a specified amount of time.
 * This action pauses the script execution for a specified number of ticks.
 */
public class WaitAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get the duration parameter from the block
            DataValue durationValue = block.getParameter("duration");
            if (durationValue == null) {
                return ExecutionResult.error("Duration parameter is missing");
            }

            // Resolve any placeholders in the duration
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedDuration = resolver.resolve(context, durationValue);
            
            // Parse duration parameter
            int ticks;
            try {
                ticks = Math.max(1, resolvedDuration.asNumber().intValue());
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid duration value");
            }

            // Pause execution for the specified number of ticks
            // Note: This is a simplified implementation. In a real implementation,
            // you would need to handle asynchronous execution properly.
            try {
                Thread.sleep(ticks * 50); // 1 tick = 50ms in Minecraft
                return ExecutionResult.success("Waited for " + ticks + " ticks");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ExecutionResult.error("Wait was interrupted");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to wait: " + e.getMessage());
        }
    }
}