package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class AddVarAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");
        DataValue rawValue = block.getParameter("value");
        
        if (rawVarName == null || rawValue == null) return;

        String varName = resolver.resolve(context, rawVarName).asString();
        String valueStr = resolver.resolve(context, rawValue).asString();

        if (varName == null || valueStr == null) return;

        try {
            // Получаем текущее значение переменной
            Object currentValue = context.getVariable(varName);
            double currentNum = 0.0;
            
            if (currentValue instanceof Number) {
                currentNum = ((Number) currentValue).doubleValue();
            }
            
            // Добавляем новое значение
            double addValue = Double.parseDouble(valueStr);
            double result = currentNum + addValue;
            
            // Сохраняем результат через контекст
            context.setVariable(varName, result);
            player.sendMessage("§a✓ Переменная '" + varName + "' увеличена на " + addValue + " = " + result);
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: значение должно быть числом");
        }
    }
} 