package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class IfVarEqualsCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return false;

        // Получаем и разрешаем параметры
        Object rawVarName = block.getParameter("variable");
        Object rawValue = block.getParameter("value");

        String varName = ParameterResolver.resolve(context, rawVarName);
        String expectedValue = ParameterResolver.resolve(context, rawValue);

        if (varName == null || expectedValue == null) return false;

        try {
            // Получаем значение переменной
            Object variableValue = context.getVariable(varName);
            
            if (variableValue == null) return false;

            // Сравниваем значения
            return variableValue.toString().equals(expectedValue);

        } catch (Exception e) {
            player.sendMessage("§cОшибка проверки ifVarEquals: " + e.getMessage());
            return false;
        }
    }
} 