package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class GetGlobalVariableAction implements BlockAction {
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
            Object globalValue = context.getPlugin().getDataManager().getPlayerVariable(player.getUniqueId(), varName);
            context.setVariable(localVarName, globalValue != null ? globalValue : "");
            player.sendMessage("§a✓ Глобальная переменная '" + varName + "' загружена в локальную '" + localVarName + "'");
        }
    }
} 