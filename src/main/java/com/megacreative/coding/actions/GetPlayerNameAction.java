package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class GetPlayerNameAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");

        String varName = rawVarName != null ? resolver.resolve(context, rawVarName).asString() : "playerName";

        String playerName = player.getName();
        context.setVariable(varName, playerName);
        
        player.sendMessage("§a👤 Имя игрока '" + playerName + "' сохранено в переменную '" + varName + "'");
    }
} 