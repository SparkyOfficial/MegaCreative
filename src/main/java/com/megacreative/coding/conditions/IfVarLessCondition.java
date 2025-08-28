package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class IfVarLessCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null || block == null || variableManager == null) return false;

        ParameterResolver resolver = new ParameterResolver(variableManager);

        try {
            // Получаем и разрешаем параметры
            DataValue rawVarName = block.getParameter("variable");
            DataValue rawValue = block.getParameter("value");

            if (rawVarName == null || rawValue == null) return false;

            DataValue varNameValue = resolver.resolve(context, rawVarName);
            DataValue compareValue = resolver.resolve(context, rawValue);

            String varName = varNameValue.asString();
            if (varName == null || varName.isEmpty()) return false;

            // Получаем значение переменной
            Object variableValue = context.getVariable(varName);
            
            if (variableValue == null) return false;

            // Пытаемся сравнить как числа
            try {
                double varNum = Double.parseDouble(variableValue.toString());
                double compareNum = Double.parseDouble(compareValue.asString());
                return varNum < compareNum;
            } catch (NumberFormatException e) {
                // Если не числа, сравниваем как строки
                return variableValue.toString().compareTo(compareValue.asString()) < 0;
            }

        } catch (Exception e) {
            player.sendMessage("§cОшибка проверки ifVarLess: " + e.getMessage());
            return false;
        }
    }
} 