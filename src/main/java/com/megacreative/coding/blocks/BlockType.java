package com.megacreative.coding.blocks;

/**
 * Перечисление типов блоков.
 */
public enum BlockType {
    // Основные типы блоков
    ACTION("Действие", "Выполняет действие"),
    CONDITION("Условие", "Проверяет условие"),
    EVENT("Событие", "Запускается по событию"),
    LOOP("Цикл", "Повторяет действия"),
    FUNCTION("Функция", "Группа действий"),
    VARIABLE("Переменная", "Хранит значение"),
    VALUE("Значение", "Возвращает значение");

    private final String displayName;
    private final String description;

    BlockType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
