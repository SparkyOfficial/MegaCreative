package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

/**
 * Action for conditional branching in scripts.
 * This action handles conditional logic and variable management.
 * 
 * Действие для условного ветвления в скриптах.
 * Это действие обрабатывает условную логику и управление переменными.
 * 
 * @author Андрій Budильников
 */
@BlockMeta(id = "conditionalBranch", displayName = "§aConditional Branch", type = BlockType.ACTION)
public class ConditionalBranchAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        
        
        
        
        String condition = block.getParameterValue("condition", String.class);
        if (condition == null) {
            condition = "default";
        }
        
        
        if (context.getPlugin() != null) {
            context.getPlugin().getLogger().fine("Executing conditional branch with condition: " + condition);
        }
        
        
        try {
            
            boolean isValid = validateCondition(block, context, condition);
            
            if (isValid) {
                
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
     * 
     * Проверяет параметр условия
     */
    private boolean validateCondition(CodeBlock block, ExecutionContext context, String condition) {
        
        
        switch (condition.toLowerCase()) {
            case "default":
            case "true":
            case "false":
                return true;
            default:
                
                return context.getVariable(condition) != null;
        }
    }
    
    /**
     * Processes the condition and performs additional actions
     * 
     * Обрабатывает условие и выполняет дополнительные действия
     */
    private void processCondition(CodeBlock block, ExecutionContext context, String condition) {
        
        switch (condition.toLowerCase()) {
            case "default":
                
                break;
            case "true":
                
                context.setVariable("lastConditionResult", true);
                break;
            case "false":
                
                context.setVariable("lastConditionResult", false);
                break;
            default:
                
                Object conditionValue = context.getVariable(condition);
                if (conditionValue != null) {
                    context.setVariable("lastConditionResult", conditionValue);
                }
                break;
        }
    }
}