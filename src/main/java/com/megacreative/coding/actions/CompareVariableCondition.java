package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;

public class CompareVariableCondition implements BlockCondition {
    @Override
    public boolean check(ExecutionContext context) {
        CodeBlock block = context.getCurrentBlock();
        if (block == null) return false;
        
        Object rawVar1 = block.getParameter("var1");
        Object rawVar2 = block.getParameter("var2");
        Object rawOperator = block.getParameter("operator");
        
        String var1Str = ParameterResolver.resolve(context, rawVar1).toString();
        String var2Str = ParameterResolver.resolve(context, rawVar2).toString();
        String operator = ParameterResolver.resolve(context, rawOperator).toString();
        
        try {
            double value1 = Double.parseDouble(var1Str);
            double value2 = Double.parseDouble(var2Str);
            
            return switch (operator) {
                case ">" -> value1 > value2;
                case "<" -> value1 < value2;
                case ">=" -> value1 >= value2;
                case "<=" -> value1 <= value2;
                case "==" -> value1 == value2;
                case "!=" -> value1 != value2;
                default -> false;
            };
        } catch (NumberFormatException e) {
            // Если не числа, сравниваем как строки
            return switch (operator) {
                case "==" -> var1Str.equals(var2Str);
                case "!=" -> !var1Str.equals(var2Str);
                default -> false;
            };
        }
    }
} 