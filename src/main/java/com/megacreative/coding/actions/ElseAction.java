package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;

/**
 * Действие для выполнения альтернативной ветки в условных конструкциях.
 * Используется в паре с условными блоками для реализации логики "иначе".
 */
@BlockMeta(id = "else", displayName = "§aElse", type = BlockType.ACTION)
public class ElseAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        
        
        context.getPlugin().getLogger().fine("Else block executed");
        return ExecutionResult.success("Else block executed");
    }
}