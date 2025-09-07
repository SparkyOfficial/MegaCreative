package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class GetGlobalVariableAction implements BlockAction {
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }
        
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");
        DataValue rawLocalVar = block.getParameter("localVar");
        
        if (rawVarName == null || rawLocalVar == null) {
            return ExecutionResult.error("Variable name or local variable name not specified");
        }

        String varName = resolver.resolve(context, rawVarName).asString();
        String localVarName = resolver.resolve(context, rawLocalVar).asString();

        if (varName != null && !varName.isEmpty() && localVarName != null && !localVarName.isEmpty()) {
            Object globalValue = context.getGlobalVariable(varName);
            context.setVariable(localVarName, globalValue != null ? globalValue : "");
            player.sendMessage("§a✓ Глобальная переменная '" + varName + "' загружена в локальную '" + localVarName + "'");
            return ExecutionResult.success("Global variable '" + varName + "' loaded to local variable '" + localVarName + "'");
        }
        
        return ExecutionResult.error("Invalid variable names");
    }
}