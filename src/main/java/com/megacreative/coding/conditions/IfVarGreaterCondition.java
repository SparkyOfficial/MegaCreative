package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class IfVarGreaterCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return false;

        // Получаем и разрешаем параметры
        Object rawVarName = block.getParameter("variable");
        Object rawValue = block.getParameter("value");

        String varName = ParameterResolver.resolve(context, rawVarName);
        String compareValue = ParameterResolver.resolve(context, rawValue);

        if (varName == null || compareValue == null) return false;

        try {
            // Получаем значение переменной
            Object variableValue = context.getVariable(varName);
            
            if (variableValue == null) return false;

            // Пытаемся сравнить как числа
            try {
                double varNum = Double.parseDouble(variableValue.toString());
                double compareNum = Double.parseDouble(compareValue);
                return varNum > compareNum;
            } catch (NumberFormatException e) {
                // Если не числа, сравниваем как строки
                return variableValue.toString().compareTo(compareValue) > 0;
            }

        } catch (Exception e) {
            player.sendMessage("§cОшибка проверки ifVarGreater: " + e.getMessage());
            return false;
        }
    }
} 