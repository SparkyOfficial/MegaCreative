package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;

/**
 * Action for getting a variable value.
 * This action retrieves variable name parameter and gets the variable value.
 */
@BlockMeta(id = "getVar", displayName = "§aGet Variable", type = BlockType.ACTION)
public class GetVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue nameValue = block.getParameter("name");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }
            
            String varName = nameValue.asString();
            
            
            DataValue varValue = context.getVariableAsDataValue(varName);
            
            if (varValue == null) {
                return ExecutionResult.error("Variable not found: " + varName);
            }
            
            
            block.setParameter("result", varValue);
            
            return ExecutionResult.success("Variable retrieved successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get variable: " + e.getMessage());
        }
    }
}