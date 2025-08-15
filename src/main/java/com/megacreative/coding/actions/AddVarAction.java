package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class AddVarAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawVarName = block.getParameter("var");
        Object rawValue = block.getParameter("value");

        String varName = ParameterResolver.resolve(context, rawVarName);
        String valueStr = ParameterResolver.resolve(context, rawValue);

        if (varName == null || valueStr == null) return;

        try {
            // Получаем текущее значение переменной
            Object currentValue = context.getVariable(varName);
            double currentNum = 0.0;
            
            if (currentValue != null) {
                try {
                    currentNum = Double.parseDouble(currentValue.toString());
                } catch (NumberFormatException e) {
                    // Если не число, начинаем с 0
                }
            }
            
            // Добавляем новое значение
            double addValue = Double.parseDouble(valueStr);
            double result = currentNum + addValue;
            
            context.setVariable(varName, result);
            player.sendMessage("§a✓ Переменная '" + varName + "' увеличена на " + addValue + " = " + result);
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: значение должно быть числом");
        }
    }
} 