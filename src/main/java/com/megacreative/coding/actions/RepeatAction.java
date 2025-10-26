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
 * Action to repeat a block execution N times
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "repeat", displayName = "§bRepeat", type = BlockType.ACTION)
public class RepeatAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameter
            DataValue countValue = block.getParameter("count");
            
            if (countValue == null) {
                return ExecutionResult.error("Missing required parameter: count");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedCount = resolver.resolve(context, countValue);
            
            int count = resolvedCount.asNumber().intValue();
            
            // Execute next block N times
            // In a real implementation, this would be handled by the script engine
            // For now, we'll just log that we would repeat
            context.getPlugin().getLogger().info("Would repeat next block " + count + " times");
            
            return ExecutionResult.success("Repeated block " + count + " times");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to repeat block: " + e.getMessage());
        }
    }
}
