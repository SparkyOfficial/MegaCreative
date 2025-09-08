package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;

/**
 * Условие для проверки равенства переменной заданному значению.
 */
public class IfVarEqualsCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawVar = block.getParameter("var");
        DataValue rawValue = block.getParameter("value");

        if (rawVar == null || rawValue == null) {
            context.getPlugin().getLogger().warning("Variable or value not specified in IfVarEqualsCondition");
            return false;
        }

        DataValue varValue = resolver.resolve(context, rawVar);
        DataValue compareValue = resolver.resolve(context, rawValue);
        
        // Compare the values
        return varValue.asString().equals(compareValue.asString());
    }
}