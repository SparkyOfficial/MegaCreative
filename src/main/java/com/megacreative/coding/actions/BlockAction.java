package com.megacreative.coding.actions;

import com.megacreative.coding.ExecutionContext;

/**
 * Интерфейс для всех исполняемых блоков кода.
 * Каждый класс, реализующий этот интерфейс, отвечает за логику одного действия.
 */
@FunctionalInterface
public interface BlockAction {
    /**
     * Выполняет логику блока.
     * @param context Контекст выполнения, содержащий игрока, мир, переменные и т.д.
     */
    void execute(ExecutionContext context);
} 