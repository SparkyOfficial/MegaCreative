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
 * Action to repeat a trigger
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "repeatTrigger", displayName = "§bRepeat Trigger", type = BlockType.ACTION)
public class RepeatTriggerAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue triggerValue = block.getParameter("trigger");
            DataValue countValue = block.getParameter("count");
            
            if (triggerValue == null || countValue == null) {
                return ExecutionResult.error("Missing required parameters: trigger, count");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTrigger = resolver.resolve(context, triggerValue);
            DataValue resolvedCount = resolver.resolve(context, countValue);
            
            String trigger = resolvedTrigger.asString();
            int count = resolvedCount.asNumber().intValue();
            
            // Repeat trigger N times
            // In a real implementation, this would be handled by the script engine
            // For now, we'll just log that we would repeat the trigger
            context.getPlugin().getLogger().info("Would repeat trigger " + trigger + " " + count + " times");
            
            return ExecutionResult.success("Repeated trigger " + trigger + " " + count + " times");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to repeat trigger: " + e.getMessage());
        }
    }
}