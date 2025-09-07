package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class SetGlobalVariableAction implements BlockAction {
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null) {
            return ExecutionResult.error("Player not available");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");
        DataValue rawValue = block.getParameter("value");

        if (rawVarName == null || rawValue == null) {
            return ExecutionResult.error("Variable name or value not specified");
        }

        DataValue varNameValue = resolver.resolve(context, rawVarName);
        DataValue value = resolver.resolve(context, rawValue);

        String varName = varNameValue.asString();
        if (varName != null && !varName.isEmpty()) {
            // Note: This should probably be setGlobalVariable, not setPlayerVariable
            variableManager.setGlobalVariable(varName, value);
            player.sendMessage("§a✓ Глобальная переменная '" + varName + "' установлена в: " + value.asString());
            return ExecutionResult.success("Global variable '" + varName + "' set to: " + value.asString());
        }
        
        return ExecutionResult.error("Invalid variable name");
    }
}