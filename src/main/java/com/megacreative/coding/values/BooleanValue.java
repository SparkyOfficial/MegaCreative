package com.megacreative.coding.values;

import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.PlaceholderResolver;

/**
 * Представляет булево значение, которое может содержать плейсхолдеры.
 */
public class BooleanValue implements Value<Boolean> {
    private final String expression;

    public BooleanValue(String expression) {
        this.expression = expression;
    }

    public BooleanValue(boolean value) {
        this.expression = String.valueOf(value);
    }

    @Override
    public Boolean get(ExecutionContext context) {
        // Обрабатываем плейсхолдеры
        String resolvedExpression = PlaceholderResolver.resolve(this.expression, context);
        
        // Приводим к нижнему регистру для сравнения
        String lowerCase = resolvedExpression.toLowerCase().trim();
        
        // Проверяем на true значения
        if (lowerCase.equals("true") || lowerCase.equals("1") || lowerCase.equals("yes") || lowerCase.equals("on")) {
            return true;
        }
        
        // Проверяем на false значения
        if (lowerCase.equals("false") || lowerCase.equals("0") || lowerCase.equals("no") || lowerCase.equals("off")) {
            return false;
        }
        
        // Если это число, проверяем на ненулевое значение
        try {
            double number = Double.parseDouble(lowerCase);
            return number != 0;
        } catch (NumberFormatException e) {
            // Если не число и не булево значение, считаем false
            return false;
        }
    }

    /**
     * Возвращает исходное выражение без обработки плейсхолдеров.
     * @return Исходное выражение
     */
    public String getRawExpression() {
        return expression;
    }
} 