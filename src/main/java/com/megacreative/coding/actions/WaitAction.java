package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta; // Added import
import com.megacreative.coding.BlockType; // Added import

/**
 * Action for waiting for a specified amount of time.
 * This action pauses execution for the specified duration.
 */
@BlockMeta(id = "wait", displayName = "§aWait", type = BlockType.ACTION) // Added annotation
public class WaitAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get duration parameter
            com.megacreative.coding.values.DataValue durationValue = block.getParameter("duration");
            
            if (durationValue == null || durationValue.isEmpty()) {
                return ExecutionResult.error("Duration is not configured");
            }
            
            long duration = Long.parseLong(durationValue.asString());
            
            // Sleep for the specified duration (in milliseconds)
            Thread.sleep(duration);
            
            return ExecutionResult.success("Wait completed successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to wait: " + e.getMessage());
        }
    }
}