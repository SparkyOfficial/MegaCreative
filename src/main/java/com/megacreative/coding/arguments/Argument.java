package com.megacreative.coding.arguments;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.Value;
import java.util.Optional;

/**
 * Интерфейс для парсеров аргументов из конфигурации блоков.
 * @param <T> Тип значения, которое может быть извлечено
 */
public interface Argument<T extends Value<?>> {
    /**
     * Пытается создать Value из конфигурации блока.
     * @param block Блок, из которого нужно извлечь данные.
     * @return Optional с Value, если парсинг успешен.
     */
    Optional<T> parse(CodeBlock block);
} 