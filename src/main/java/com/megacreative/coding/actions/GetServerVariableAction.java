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

        if (player == null || block == null) return;

        ParameterResolver resolver = new ParameterResolver(context);
        VariableManager variableManager = context.getPlugin().getVariableManager();

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");
        DataValue rawLocalVar = block.getParameter("localVar");

        if (rawVarName == null || rawLocalVar == null) return;

        String varName = resolver.resolve(context, rawVarName).asString();
        String localVarName = resolver.resolve(context, rawLocalVar).asString();

        if (varName != null && !varName.isEmpty() && localVarName != null && !localVarName.isEmpty()) {
            DataValue serverValue = variableManager.getPersistentVariable(varName);
            context.setVariable(localVarName, serverValue != null ? serverValue.getValue() : "");
            player.sendMessage("§a✓ Серверная переменная '" + varName + "' загружена в локальную '" + localVarName + "'");
        }
    }
} 