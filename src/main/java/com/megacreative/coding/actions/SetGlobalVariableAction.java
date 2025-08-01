package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class SetGlobalVariableAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawVarName = block.getParameter("var");
        Object rawValue = block.getParameter("value");

        String varName = ParameterResolver.resolve(context, rawVarName);
        String value = ParameterResolver.resolve(context, rawValue);

        if (varName != null && !varName.isEmpty()) {
            context.getPlugin().getDataManager().setPlayerVariable(player.getUniqueId(), varName, value);
            player.sendMessage("§a✓ Глобальная переменная '" + varName + "' установлена в: " + value);
        }
    }
} 