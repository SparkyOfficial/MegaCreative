package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class GetServerVariableAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawVarName = block.getParameter("var");
        Object rawLocalVar = block.getParameter("localVar");

        String varName = ParameterResolver.resolve(context, rawVarName);
        String localVarName = ParameterResolver.resolve(context, rawLocalVar);

        if (varName != null && !varName.isEmpty() && localVarName != null && !localVarName.isEmpty()) {
            Object serverValue = context.getPlugin().getDataManager().getServerVariable(varName);
            context.setVariable(localVarName, serverValue != null ? serverValue : "");
            player.sendMessage("§a✓ Серверная переменная '" + varName + "' загружена в локальную '" + localVarName + "'");
        }
    }
} 