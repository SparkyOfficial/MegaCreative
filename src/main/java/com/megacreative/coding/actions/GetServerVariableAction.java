package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class GetServerVariableAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null || block == null || variableManager == null) return;

        ParameterResolver resolver = new ParameterResolver(variableManager);

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");
        DataValue rawLocalVar = block.getParameter("localVar");

        if (rawVarName == null || rawLocalVar == null) return;

        DataValue varNameValue = resolver.resolve(context, rawVarName);
        DataValue localVarValue = resolver.resolve(context, rawLocalVar);

        String varName = varNameValue.asString();
        String localVarName = localVarValue.asString();

        if (varName != null && !varName.isEmpty() && localVarName != null && !localVarName.isEmpty()) {
            Object serverValue = variableManager.getServerVariable(varName);
            context.setVariable(localVarName, serverValue != null ? serverValue : "");
            player.sendMessage("§a✓ Серверная переменная '" + varName + "' загружена в локальную '" + localVarName + "'");
        }
    }
} 