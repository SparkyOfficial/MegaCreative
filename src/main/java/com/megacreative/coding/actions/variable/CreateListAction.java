package com.megacreative.coding.actions.variable;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ListValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class CreateListAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the block
            DataValue listNameValue = block.getParameter("list_name");
            DataValue initialValuesValue = block.getParameter("initial_values");
            
            if (listNameValue == null || listNameValue.isEmpty()) {
                return ExecutionResult.error("List name parameter is missing.");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedListName = resolver.resolve(context, listNameValue);
            
            String listName = resolvedListName.asString();
            
            // Create the list
            List<DataValue> initialValues = new ArrayList<>();
            if (initialValuesValue != null && !initialValuesValue.isEmpty()) {
                DataValue resolvedInitialValues = resolver.resolve(context, initialValuesValue);
                if (resolvedInitialValues instanceof ListValue) {
                    initialValues = ((ListValue) resolvedInitialValues).getList(); // Changed from getValues() to getList()
                }
            }
            
            ListValue listValue = new ListValue(initialValues);
            
            // Store the list in the variable manager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                variableManager.setVariable(listName, listValue, VariableScope.LOCAL, context.getScriptId());
            }
            
            return ExecutionResult.success("List '" + listName + "' created.");

        } catch (Exception e) {
            return ExecutionResult.error("Error creating list: " + e.getMessage());
        }
    }
}