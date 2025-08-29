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
            // Detect parameter format: new (var1, operator, var2) vs legacy (variable, value)
            DataValue rawVar1 = block.getParameter("var1");
            DataValue rawOperator = block.getParameter("operator");
            DataValue rawVar2 = block.getParameter("var2");
            
            // Legacy format support for backward compatibility
            DataValue rawVariable = block.getParameter("variable");
            DataValue rawValue = block.getParameter("value");
            
            if (rawVar1 != null && rawOperator != null && rawVar2 != null) {
                // New format: var1 operator var2
                return evaluateNewFormat(context, resolver, rawVar1, rawOperator, rawVar2);
            } else if (rawVariable != null && rawValue != null) {
                // Legacy format: determine operator from calling context
                String action = block.getAction();
                String operator = getOperatorFromAction(action);
                return evaluateLegacyFormat(context, resolver, rawVariable, rawValue, operator);
            } else {
                player.sendMessage("§cОшибка: неверные параметры для сравнения переменных");
                return false;
            }

        } catch (Exception e) {
            player.sendMessage("§cОшибка сравнения переменных: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Handle new format: var1 operator var2
     */
    private boolean evaluateNewFormat(ExecutionContext context, ParameterResolver resolver, 
                                     DataValue rawVar1, DataValue rawOperator, DataValue rawVar2) {
        DataValue var1Value = resolver.resolve(context, rawVar1);
        DataValue operatorValue = resolver.resolve(context, rawOperator);
        DataValue var2Value = resolver.resolve(context, rawVar2);

        String var1Str = var1Value.asString();
        String operatorStr = operatorValue.asString();
        String var2Str = var2Value.asString();

        if (var1Str == null || operatorStr == null || var2Str == null) return false;

        // Получаем значения переменных через VariableManager для типобезопасности
        VariableManager variableManager = context.getPlugin().getVariableManager();
        DataValue value1Obj = variableManager.getVariable(var1Str, context.getScriptId(), context.getWorldId());
        DataValue value2Obj = variableManager.getVariable(var2Str, context.getScriptId(), context.getWorldId());

        if (value1Obj == null || value1Obj.isEmpty() || value2Obj == null || value2Obj.isEmpty()) return false;

        // Пытаемся сравнить как числа
        try {
            double num1 = value1Obj.asNumber().doubleValue();
            double num2 = value2Obj.asNumber().doubleValue();
            return compareNumbers(num1, operatorStr, num2);
        } catch (NumberFormatException e) {
            // Если не числа, сравниваем как строки
            return compareStrings(value1Obj.asString(), operatorStr, value2Obj.asString());
        }
    }
    
    /**
     * Handle legacy format: variable operator value
     */
    private boolean evaluateLegacyFormat(ExecutionContext context, ParameterResolver resolver,
                                        DataValue rawVariable, DataValue rawValue, String operator) {
        DataValue varNameValue = resolver.resolve(context, rawVariable);
        DataValue expectedValue = resolver.resolve(context, rawValue);

        String varName = varNameValue.asString();
        if (varName == null || varName.isEmpty()) return false;

        // Получаем значение переменной через VariableManager для типобезопасности
        VariableManager variableManager = context.getPlugin().getVariableManager();
        DataValue variableValue = variableManager.getVariable(varName, context.getScriptId(), context.getWorldId());
        
        if (variableValue == null || variableValue.isEmpty()) return false;

        // Применяем оператор
        try {
            double varNum = variableValue.asNumber().doubleValue();
            double compareNum = expectedValue.asNumber().doubleValue();
            return compareNumbers(varNum, operator, compareNum);
        } catch (NumberFormatException e) {
            // Если не числа, сравниваем как строки
            return compareStrings(variableValue.asString(), operator, expectedValue.asString());
        }
    }
    
    /**
     * Map legacy action names to operators
     */
    private String getOperatorFromAction(String action) {
        return switch (action) {
            case "ifVarEquals" -> "==";
            case "ifVarGreater" -> ">";
            case "ifVarLess" -> "<";
            default -> "=="; // default to equals
        };
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