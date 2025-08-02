package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;

public class CompareVariableCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        CodeBlock block = context.getCurrentBlock();
        if (block == null) return false;
        
        Object rawVar1 = block.getParameter("var1");
        Object rawVar2 = block.getParameter("var2");
        Object rawOperator = block.getParameter("operator");
        
        String var1Str = ParameterResolver.resolve(context, rawVar1);
        String var2Str = ParameterResolver.resolve(context, rawVar2);
        String operator = ParameterResolver.resolve(context, rawOperator);
        
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