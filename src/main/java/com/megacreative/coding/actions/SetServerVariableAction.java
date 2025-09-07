package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class SetServerVariableAction implements BlockAction {
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (context == null) {
            return ExecutionResult.error("Context is null");
        }
        
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
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
            context.setServerVariable(varName, value);
            player.sendMessage("§a✓ Серверная переменная '" + varName + "' установлена в: " + value.asString());
            return ExecutionResult.success("Server variable '" + varName + "' set to: " + value.asString());
        }
        
        return ExecutionResult.error("Invalid variable name");
    }
}