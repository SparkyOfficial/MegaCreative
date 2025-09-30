package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;

/**
 * Action for waiting for a specified amount of time.
 * This action pauses execution for the specified duration.
 */
@BlockMeta(id = "wait", displayName = "§aWait", type = BlockType.ACTION)
public class WaitAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get duration parameter
            DataValue durationValue = block.getParameter("duration");
            
            if (durationValue == null || durationValue.isEmpty()) {
                return ExecutionResult.error("Duration is not configured");
            }
            
            long duration = Long.parseLong(durationValue.asString());
            
            // Convert milliseconds to ticks (1 tick = 50 milliseconds)
            long ticks = Math.max(1, duration / 50);
            
            // Return a pause result - the ScriptEngine will handle scheduling
            return ExecutionResult.pause(ticks);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to wait: " + e.getMessage());
        }
    }
}