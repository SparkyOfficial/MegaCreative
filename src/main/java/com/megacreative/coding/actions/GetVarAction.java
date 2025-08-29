package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class GetVarAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");
        if (rawVarName == null) return;

        String varName = resolver.resolve(context, rawVarName).asString();
        if (varName == null) return;

        // Получаем значение переменной через контекст
        Object value = context.getVariable(varName);
        String displayValue = value != null ? value.toString() : "не установлена";
        
        player.sendMessage("§a📖 Переменная '" + varName + "' = " + displayValue);
    }
} 