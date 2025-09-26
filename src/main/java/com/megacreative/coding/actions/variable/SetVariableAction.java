package com.megacreative.coding.actions.variable;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

@BlockMeta(id = "setVariable", displayName = "§aSet Variable", type = BlockType.ACTION)
public class SetVariableAction implements BlockAction {
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();

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
            context.setVariable(varName, value);
            player.sendMessage("§a✓ Переменная '" + varName + "' установлена в: " + value.asString());
            return ExecutionResult.success("Variable '" + varName + "' set to: " + value.asString());
        }
        
        return ExecutionResult.error("Invalid variable name");
    }
}