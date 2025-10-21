package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Action for creating a list variable.
 * This action creates a new list variable with optional initial values.
 * 
 * Действие для создания переменной списка.
 * Это действие создает новую переменную списка с необязательными начальными значениями.
 * 
 * @author Андрій Budильников
 */
@BlockMeta(id = "createList", displayName = "§aCreate List", type = BlockType.ACTION)
public class CreateListAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            
            DataValue listNameValue = block.getParameter("list_name");
            DataValue initialValuesValue = block.getParameter("initial_values");
            
            if (listNameValue == null || listNameValue.isEmpty()) {
                return ExecutionResult.error("List name parameter is missing.");
            }
            
            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedListName = resolver.resolve(context, listNameValue);
            
            String listName = resolvedListName.asString();
            
            
            List<DataValue> initialValues = new ArrayList<>();
            if (initialValuesValue != null && !initialValuesValue.isEmpty()) {
                DataValue resolvedInitialValues = resolver.resolve(context, initialValuesValue);
                if (resolvedInitialValues instanceof ListValue listValue) {
                    initialValues = listValue.getValues();
                }
            }
            
            ListValue listValue = new ListValue(initialValues);
            
            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            if (variableManager != null) {
                variableManager.setVariable(listName, listValue, VariableScope.LOCAL, context.getScriptId());
            }
            
            return ExecutionResult.success("List '" + listName + "' created.");

        } catch (Exception e) {
            return ExecutionResult.error("Error creating list: " + e.getMessage());
        }
    }
}