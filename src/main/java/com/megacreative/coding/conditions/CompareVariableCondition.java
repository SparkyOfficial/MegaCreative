package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class CompareVariableCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null || block == null || variableManager == null) return false;

        ParameterResolver resolver = new ParameterResolver(variableManager);

        try {
            // Получаем и разрешаем параметры
            DataValue rawVar1 = block.getParameter("var1");
            DataValue rawOperator = block.getParameter("operator");
            DataValue rawVar2 = block.getParameter("var2");

            if (rawVar1 == null || rawOperator == null || rawVar2 == null) return false;

            DataValue var1Value = resolver.resolve(context, rawVar1);
            DataValue operatorValue = resolver.resolve(context, rawOperator);
            DataValue var2Value = resolver.resolve(context, rawVar2);

            String var1Str = var1Value.asString();
            String operatorStr = operatorValue.asString();
            String var2Str = var2Value.asString();

            if (var1Str == null || operatorStr == null || var2Str == null) return false;

            // Получаем значения переменных
            Object value1 = context.getVariable(var1Str);
            Object value2 = context.getVariable(var2Str);

            if (value1 == null || value2 == null) return false;

            // Пытаемся сравнить как числа
            try {
                double num1 = Double.parseDouble(value1.toString());
                double num2 = Double.parseDouble(value2.toString());
                return compareNumbers(num1, operatorStr, num2);
            } catch (NumberFormatException e) {
                // Если не числа, сравниваем как строки
                return compareStrings(value1.toString(), operatorStr, value2.toString());
            }

        } catch (Exception e) {
            player.sendMessage("§cОшибка сравнения переменных: " + e.getMessage());
            return false;
        }
    }

    private boolean compareNumbers(double num1, String operator, double num2) {
        switch (operator.toLowerCase()) {
            case "==":
            case "=":
                return num1 == num2;
            case "!=":
            case "<>":
                return num1 != num2;
            case ">":
                return num1 > num2;
            case ">=":
                return num1 >= num2;
            case "<":
                return num1 < num2;
            case "<=":
                return num1 <= num2;
            default:
                return false;
        }
    }

    private boolean compareStrings(String str1, String operator, String str2) {
        switch (operator.toLowerCase()) {
            case "==":
            case "=":
                return str1.equals(str2);
            case "!=":
            case "<>":
                return !str1.equals(str2);
            case "contains":
                return str1.contains(str2);
            case "startswith":
                return str1.startsWith(str2);
            case "endswith":
                return str1.endsWith(str2);
            default:
                return false;
        }
    }
} 