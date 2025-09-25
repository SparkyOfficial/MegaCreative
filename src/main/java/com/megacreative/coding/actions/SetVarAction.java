package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

/**
 * Action for setting a variable value.
 * This action retrieves variable name and value parameters and sets the variable.
 */
@BlockMeta(id = "setVar", displayName = "§aSet Variable", type = BlockType.ACTION)
public class SetVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get variable name and value parameters
            com.megacreative.coding.values.DataValue nameValue = block.getParameter("name");
            com.megacreative.coding.values.DataValue valueValue = block.getParameter("value");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }
            
            String varName = nameValue.asString();
            com.megacreative.coding.values.DataValue varValue = valueValue;
            
            // Set the variable in the context
            context.setVariable(varName, varValue);
            
            return ExecutionResult.success("Variable set successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set variable: " + e.getMessage());
        }
    }
}