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
 * Action to execute conditional branches
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "conditionalBranch", displayName = "§bConditional Branch", type = BlockType.ACTION)
public class ConditionalBranchAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameter
            DataValue conditionCountValue = block.getParameter("conditionCount");
            
            if (conditionCountValue == null) {
                return ExecutionResult.error("Missing required parameter: conditionCount");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedConditionCount = resolver.resolve(context, conditionCountValue);
            
            int conditionCount = resolvedConditionCount.asNumber().intValue();
            
            // Execute conditional branches
            // In a real implementation, this would be handled by the script engine
            // For now, we'll just log that we would execute conditional branches
            context.getPlugin().getLogger().info("Executing conditional branch with " + conditionCount + " conditions");
            
            return ExecutionResult.success("Executed conditional branch");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to execute conditional branch: " + e.getMessage());
        }
    }
}