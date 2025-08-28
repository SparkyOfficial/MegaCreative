package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;

public class CompareVariableCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        CodeBlock block = context.getCurrentBlock();
        if (block == null) return false;
        
        VariableManager variableManager = context.getPlugin().getVariableManager();
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
        DataValue rawVar1 = block.getParameter("var1");
        DataValue rawVar2 = block.getParameter("var2");
        DataValue rawOperator = block.getParameter("operator");
        
        String var1Str = resolver.resolve(context, rawVar1).asString();
        String var2Str = resolver.resolve(context, rawVar2).asString();
        String operator = resolver.resolve(context, rawOperator).asString();
        
        if (var1Str == null || var2Str == null || operator == null) return false;
        
        try {
            double value1 = Double.parseDouble(var1Str);
            double value2 = Double.parseDouble(var2Str);
            
            switch (operator) {
                case ">":
                    return value1 > value2;
                case "<":
                    return value1 < value2;
                case ">=":
                    return value1 >= value2;
                case "<=":
                    return value1 <= value2;
                case "==":
                    return value1 == value2;
                case "!=":
                    return value1 != value2;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            // Если не числа, сравниваем как строки
            switch (operator) {
                case "==":
                    return var1Str.equals(var2Str);
                case "!=":
                    return !var1Str.equals(var2Str);
                default:
                    return false;
            }
        }
    }
} 