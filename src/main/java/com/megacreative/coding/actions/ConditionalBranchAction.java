package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

@BlockMeta(id = "conditionalBranch", displayName = "§aConditional Branch", type = BlockType.ACTION)
public class ConditionalBranchAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        // This action is primarily handled in the ScriptEngine's processBlock method
        // But we can add additional logic here if needed
        
        // Get condition parameter
        String condition = block.getParameterValue("condition", String.class);
        if (condition == null) {
            condition = "default";
        }
        
        // Log the conditional branch execution for debugging purposes
        if (context.getPlugin() != null) {
            context.getPlugin().getLogger().fine("Executing conditional branch with condition: " + condition);
        }
        
        // Add more sophisticated conditional branch handling logic
        // This could include logging, debugging, or additional validation
        try {
            // Perform additional validation or processing
            boolean isValid = validateCondition(block, context, condition);
            
            if (isValid) {
                // Additional processing based on condition type
                processCondition(block, context, condition);
                
                return ExecutionResult.success("Conditional branch processed successfully with condition: " + condition);
            } else {
                return ExecutionResult.error("Invalid condition: " + condition);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to process conditional branch: " + e.getMessage());
        }
    }
    
    /**
     * Validates the condition parameter
     */
    private boolean validateCondition(CodeBlock block, ExecutionContext context, String condition) {
        // Add validation logic based on the condition
        // For example, check if required parameters are present
        switch (condition.toLowerCase()) {
            case "default":
            case "true":
            case "false":
                return true;
            default:
                // Check if it's a custom condition
                return context.getVariable(condition) != null;
        }
    }
    
    /**
     * Processes the condition and performs additional actions
     */
    private void processCondition(CodeBlock block, ExecutionContext context, String condition) {
        // Add processing logic based on the condition
        switch (condition.toLowerCase()) {
            case "default":
                // Default processing
                break;
            case "true":
                // True condition processing
                context.setVariable("lastConditionResult", true);
                break;
            case "false":
                // False condition processing
                context.setVariable("lastConditionResult", false);
                break;
            default:
                // Custom condition processing
                Object conditionValue = context.getVariable(condition);
                if (conditionValue != null) {
                    context.setVariable("lastConditionResult", conditionValue);
                }
                break;
        }
    }
}