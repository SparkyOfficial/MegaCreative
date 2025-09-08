package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;

/**
 * Условие для сравнения значений переменных.
 */
public class CompareVariableCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawVar1 = block.getParameter("var1");
        DataValue rawVar2 = block.getParameter("var2");
        DataValue rawOperator = block.getParameter("operator");

        if (rawVar1 == null || rawVar2 == null) {
            context.getPlugin().getLogger().warning("Variables not specified in CompareVariableCondition");
            return false;
        }

        DataValue var1Value = resolver.resolve(context, rawVar1);
        DataValue var2Value = resolver.resolve(context, rawVar2);
        DataValue operatorValue = rawOperator != null ? resolver.resolve(context, rawOperator) : null;

        String operator = operatorValue != null ? operatorValue.asString() : "==";
        
        try {
            // Convert both values to strings for comparison
            String value1 = var1Value.asString();
            String value2 = var2Value.asString();
            
            // Try to compare as numbers if possible
            try {
                double num1 = Double.parseDouble(value1);
                double num2 = Double.parseDouble(value2);
                
                switch (operator) {
                    case ">":
                        return num1 > num2;
                    case ">=":
                        return num1 >= num2;
                    case "<":
                        return num1 < num2;
                    case "<=":
                        return num1 <= num2;
                    case "!=":
                        return num1 != num2;
                    case "==":
                    default:
                        return num1 == num2;
                }
            } catch (NumberFormatException e) {
                // If not numbers, compare as strings
                switch (operator) {
                    case "!=":
                        return !value1.equals(value2);
                    case "==":
                    default:
                        return value1.equals(value2);
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error in CompareVariableCondition: " + e.getMessage());
            return false;
        }
    }
}