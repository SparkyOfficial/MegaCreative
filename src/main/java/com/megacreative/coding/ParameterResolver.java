package com.megacreative.coding;

import com.megacreative.coding.data.DataType;

public class ParameterResolver {
    public static String resolve(ExecutionContext context, Object parameterValue) {
        if (context == null) return parameterValue != null ? parameterValue.toString() : "";
        
        // Если значение не строка или не начинается с "data:", возвращаем как есть.
        if (!(parameterValue instanceof String strValue) || !strValue.startsWith("data:")) {
            return parameterValue != null ? parameterValue.toString() : "";
        }

        String[] parts = strValue.split(":", 3);
        if (parts.length < 3) return strValue; // Неверный формат

        try {
            DataType type = DataType.valueOf(parts[1]);
            String valueKey = parts[2];

            if (type == DataType.VARIABLE) {
                // Ищем значение переменной в контексте. Если нет, возвращаем пустую строку.
                Object resolvedVar = context.getVariable(valueKey);
                return resolvedVar != null ? resolvedVar.toString() : "";
            }
            
            // Для TEXT и NUMBER просто возвращаем их значение.
            return valueKey;
        } catch (IllegalArgumentException e) {
            return strValue; // Неизвестный тип данных, возвращаем как есть
        }
    }
} 