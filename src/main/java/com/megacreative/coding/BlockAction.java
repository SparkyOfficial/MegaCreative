package com.megacreative.coding;

/**
 * Интерфейс для действий блоков кода.
 * Реализуется классами, которые выполняют различные действия во время выполнения скрипта.
 */
public interface BlockAction {
    
    /**
     * Выполняет действие блока.
     * @param context Контекст выполнения скрипта
     */
    void execute(ExecutionContext context);
} 