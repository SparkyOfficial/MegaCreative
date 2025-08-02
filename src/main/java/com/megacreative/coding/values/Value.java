package com.megacreative.coding.values;

import com.megacreative.coding.ExecutionContext;

/**
 * Интерфейс для представления значений, которые могут быть вычислены в контексте выполнения.
 * @param <T> Тип значения
 */
public interface Value<T> {
    /**
     * Вычисляет и возвращает конкретное значение в данном контексте.
     * @param context Контекст выполнения скрипта.
     * @return Вычисленное значение.
     */
    T get(ExecutionContext context);
} 