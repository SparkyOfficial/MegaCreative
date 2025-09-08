package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;

/**
 * Условие для проверки, что переменная меньше заданного значения.
 */
public class IfVarLessCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawVar = block.getParameter("var");
        DataValue rawValue = block.getParameter("value");

        if (rawVar == null || rawValue == null) {
            context.getPlugin().getLogger().warning("Variable or value not specified in IfVarLessCondition");
            return false;
        }

        DataValue varValue = resolver.resolve(context, rawVar);
        DataValue compareValue = resolver.resolve(context, rawValue);
        
        try {
            // Try to compare as numbers
            double varNum = Double.parseDouble(varValue.asString());
            double compareNum = Double.parseDouble(compareValue.asString());
            return varNum < compareNum;
        } catch (NumberFormatException e) {
            // If not numbers, compare as strings length
            return varValue.asString().length() < compareValue.asString().length();
        }
    }
}