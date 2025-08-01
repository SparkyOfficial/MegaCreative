package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class GetPlayerNameAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawVarName = block.getParameter("var");

        String varName = ParameterResolver.resolve(context, rawVarName);

        if (varName == null) {
            varName = "playerName"; // Значение по умолчанию
        }

        String playerName = player.getName();
        context.setVariable(varName, playerName);
        
        player.sendMessage("§a👤 Имя игрока '" + playerName + "' сохранено в переменную '" + varName + "'");
    }
} 