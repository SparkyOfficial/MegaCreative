package com.megacreative.coding.values;

import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.PlaceholderResolver;

/**
 * Представляет текстовое значение, которое может содержать плейсхолдеры.
 */
public class TextValue implements Value<String> {
    private final String text;

    public TextValue(String text) {
        this.text = text;
    }

    @Override
    public String get(ExecutionContext context) {
        // Используем PlaceholderResolver для обработки плейсхолдеров
        return PlaceholderResolver.resolve(this.text, context);
    }

    /**
     * Возвращает исходный текст без обработки плейсхолдеров.
     * @return Исходный текст
     */
    public String getRawText() {
        return text;
    }
} 