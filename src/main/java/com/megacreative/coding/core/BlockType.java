package com.megacreative.coding.core;

/**
 * Перечисление типов блоков визуального программирования.
 */
public enum BlockType {
    // Основные типы
    EVENT,       // Блок-событие (начало выполнения)
    ACTION,      // Блок-действие
    CONDITION,   // Блок-условие
    LOOP,        // Блок-цикл
    FUNCTION,    // Блок-функция
    VARIABLE,    // Блок-переменная
    
    // Специальные типы
    CONTAINER,   // Контейнер для других блоков
    SEPARATOR,   // Разделитель
    COMMENT,     // Комментарий
    
    // Системные типы
    TRIGGER,     // Триггер (специальные события)
    DEBUG,       // Отладочный блок
    
    // Группы блоков
    GROUP;       // Группа блоков
    
    /**
     * Проверяет, является ли блок исполняемым.
     */
    public boolean isExecutable() {
        return this == EVENT || this == ACTION || this == CONDITION || 
               this == LOOP || this == FUNCTION || this == TRIGGER;
    }
    
    /**
     * Проверяет, может ли блок содержать дочерние блоки.
     */
    public boolean canHaveChildren() {
        return this == CONTAINER || this == CONDITION || this == LOOP || 
               this == FUNCTION || this == GROUP;
    }
    
    /**
     * Проверяет, является ли блок структурным.
     */
    public boolean isStructural() {
        return this == CONDITION || this == LOOP || this == FUNCTION || 
               this == CONTAINER || this == GROUP;
    }
}
