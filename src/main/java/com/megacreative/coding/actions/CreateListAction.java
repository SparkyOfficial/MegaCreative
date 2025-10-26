package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Action to create a list variable
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "createList", displayName = "§bCreate List", type = BlockType.ACTION)
public class CreateListAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameter
            DataValue listNameValue = block.getParameter("listName");
            
            if (listNameValue == null) {
                return ExecutionResult.error("Missing required parameter: listName");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedListName = resolver.resolve(context, listNameValue);
            
            String listName = resolvedListName.asString();
            
            // Create empty list
            List<Object> list = new ArrayList<>();
            
            // Set list variable
            context.setVariable(listName, list);
            
            return ExecutionResult.success("Created list " + listName);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create list: " + e.getMessage());
        }
    }
}