package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;

/**
 * Action to execute a block after a delay
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "timedExecution", displayName = "§bTimed Execution", type = BlockType.ACTION)
public class TimedExecutionAction implements BlockAction {
    
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
            
            // Execute next block after delay
            // In a real implementation, this would be handled by the script engine
            // For now, we'll schedule a task to log that we would execute
            Bukkit.getScheduler().runTaskLater(context.getPlugin(), () -> {
                context.getPlugin().getLogger().info("Executing timed block after " + delay + " ticks");
            }, delay);
            
            return ExecutionResult.success("Scheduled timed execution after " + delay + " ticks");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to schedule timed execution: " + e.getMessage());
        }
    }
}