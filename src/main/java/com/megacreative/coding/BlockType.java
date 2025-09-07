package com.megacreative.coding;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Простое перечисление для классификации типов блоков, используемое в коде.
 * НЕ содержит информации о материалах или конкретных действиях.
 * Заменяет старую, жестко закодированную систему.
 */
public enum BlockType {
    EVENT,
    ACTION,
    CONDITION,
    CONTROL,
    FUNCTION,
    INTEGRATION,
    DATA,
    DEBUG,
    UNKNOWN;

    /**
     * Преобразует строковый тип из YAML в enum.
     * @param typeString Строка типа из файла конфигурации (например, "ACTION").
     * @return Соответствующий BlockType или UNKNOWN, если не найден.
     */
    public static BlockType fromString(String typeString) {
        if (typeString == null) {
            return UNKNOWN;
        }
        try {
            return BlockType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}