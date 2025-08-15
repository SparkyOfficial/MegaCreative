package com.megacreative.coding.values;

import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.PlaceholderResolver;

/**
 * Представляет числовое значение, которое может содержать плейсхолдеры.
 */
public class NumberValue implements Value<Double> {
    private final String expression;

    public NumberValue(String expression) {
        this.expression = expression;
    }

    public NumberValue(double value) {
        this.expression = String.valueOf(value);
    }

    @Override
    public Double get(ExecutionContext context) {
        // Сначала обрабатываем плейсхолдеры
        String resolvedExpression = PlaceholderResolver.resolve(this.expression, context);
        
        try {
            // Пытаемся вычислить выражение
            return evaluateExpression(resolvedExpression);
        } catch (NumberFormatException e) {
            // Если не удалось вычислить, возвращаем 0
            return 0.0;
        }
    }

    /**
     * Вычисляет математическое выражение.
     * @param expression Выражение для вычисления
     * @return Результат вычисления
     */
    private double evaluateExpression(String expression) {
        // Простая реализация для базовых операций
        // В будущем можно заменить на более продвинутый парсер
        expression = expression.trim();
        
        // Если это просто число
        if (expression.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(expression);
        }
        
        // Простые операции: +, -, *, /
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+", 2);
            return evaluateExpression(parts[0]) + evaluateExpression(parts[1]);
        }
        if (expression.contains("-")) {
            String[] parts = expression.split("-", 2);
            return evaluateExpression(parts[0]) - evaluateExpression(parts[1]);
        }
        if (expression.contains("*")) {
            String[] parts = expression.split("\\*", 2);
            return evaluateExpression(parts[0]) * evaluateExpression(parts[1]);
        }
        if (expression.contains("/")) {
            String[] parts = expression.split("/", 2);
            double divisor = evaluateExpression(parts[1]);
            if (divisor == 0) return 0;
            return evaluateExpression(parts[0]) / divisor;
        }
        
        // Если ничего не подошло, пытаемся парсить как число
        return Double.parseDouble(expression);
    }

    /**
     * Возвращает исходное выражение без обработки плейсхолдеров.
     * @return Исходное выражение
     */
    public String getRawExpression() {
        return expression;
    }
} 