package com.megacreative.coding;

import com.megacreative.coding.data.DataType;

public class ParameterResolver {
    public static Object resolve(ExecutionContext context, Object parameterValue) {
        if (parameterValue instanceof String strValue && strValue.startsWith("data:")) {
            String[] parts = strValue.split(":", 3); // data:TYPE:value
            if (parts.length == 3) {
                try {
                    DataType type = DataType.valueOf(parts[1]);
                    String value = parts[2];

                    if (type == DataType.VARIABLE) {
                        return context.getVariable(value); // Возвращаем значение переменной из контекста
                    }
                    return value; // Для TEXT и NUMBER просто возвращаем значение
                } catch (IllegalArgumentException e) {
                    // Если тип данных неизвестен, возвращаем исходное значение
                    return parameterValue;
                }
            }
        }
        return parameterValue; // Это обычное, статичное значение
    }
} 