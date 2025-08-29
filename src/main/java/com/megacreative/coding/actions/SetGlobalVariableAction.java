package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class SetGlobalVariableAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null || block == null || variableManager == null) return;

        ParameterResolver resolver = new ParameterResolver(variableManager);

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");
        DataValue rawValue = block.getParameter("value");

        if (rawVarName == null || rawValue == null) return;

        DataValue varNameValue = resolver.resolve(context, rawVarName);
        DataValue value = resolver.resolve(context, rawValue);

        String varName = varNameValue.asString();
        if (varName != null && !varName.isEmpty()) {
            variableManager.setPlayerVariable(player.getUniqueId(), varName, value.getValue());
            player.sendMessage("§a✓ Глобальная переменная '" + varName + "' установлена в: " + value.asString());
        }
    }
} 