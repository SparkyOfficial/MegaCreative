package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class GetVariableAction implements BlockAction {
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null) {
            return ExecutionResult.error("Player not available");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");

        if (rawVarName == null) {
            return ExecutionResult.error("Variable name not specified");
        }

        DataValue varNameValue = resolver.resolve(context, rawVarName);
        String varName = varNameValue.asString();

        if (varName != null && !varName.isEmpty()) {
            Object value = context.getVariable(varName);
            if (value != null) {
                player.sendMessage("§a✓ Значение переменной '" + varName + "': " + value.toString());
                return ExecutionResult.success("Variable '" + varName + "' value: " + value.toString());
            } else {
                player.sendMessage("§c❌ Переменная '" + varName + "' не найдена или пуста");
                return ExecutionResult.error("Variable '" + varName + "' not found or empty");
            }
        }
        
        return ExecutionResult.error("Invalid variable name");
    }
}