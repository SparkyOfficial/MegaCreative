package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;

/**
 * Condition for checking if a variable equals a specific value.
 * This condition returns true if the variable equals the specified value, false otherwise.
 */
@BlockMeta(id = "ifVarEquals", displayName = "§aIf Variable Equals", type = BlockType.CONDITION)
public class IfVarEqualsCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        try {
            // Get variable name and expected value parameters
            DataValue nameValue = block.getParameter("name");
            DataValue expectedValue = block.getParameter("value");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return false;
            }
            
            String varName = nameValue.asString();
            
            // Get the variable from the context as DataValue
            DataValue varValue = context.getVariableAsDataValue(varName);
            
            if (varValue == null || expectedValue == null) {
                return false;
            }
            
            // Compare values
            return varValue.asString().equals(expectedValue.asString());
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}