package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;

/**
 * Action to execute an alternative block when a condition is false
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "else", displayName = "§bElse", type = BlockType.ACTION)
public class ElseAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // This is a control flow action that would be handled by the script engine
            // For now, we'll just log that we're executing the else block
            context.getPlugin().getLogger().info("Executing else block");
            
            return ExecutionResult.success("Executed else block");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to execute else block: " + e.getMessage());
        }
    }
}