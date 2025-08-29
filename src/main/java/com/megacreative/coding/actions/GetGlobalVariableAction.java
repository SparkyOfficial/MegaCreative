package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class GetGlobalVariableAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");
        DataValue rawLocalVar = block.getParameter("localVar");
        
        if (rawVarName == null || rawLocalVar == null) return;

        String varName = resolver.resolve(rawVarName).asString();
        String localVarName = resolver.resolve(rawLocalVar).asString();

        if (varName != null && !varName.isEmpty() && localVarName != null && !localVarName.isEmpty()) {
            Object globalValue = context.getGlobalVariable(varName);
            context.setVariable(localVarName, globalValue != null ? globalValue : "");
            player.sendMessage("§a✓ Глобальная переменная '" + varName + "' загружена в локальную '" + localVarName + "'");
        }
    }
} 