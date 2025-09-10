package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;

public class ConditionalBranchAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        // This action is primarily handled in the ScriptEngine's processBlock method
        // But we can add additional logic here if needed
        
        // For now, we just return success as the control flow is handled by the engine
        return ExecutionResult.success("Conditional branch processed");
    }
}
