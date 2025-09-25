package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;

/**
 * Интерфейс для действий блоков кода.
 * Реализуется классами, которые выполняют различные действия во время выполнения скрипта.
 */
public interface BlockAction {
    
    /**
     * Выполняет действие блока.
     * @param block Блок кода для выполнения
     * @param context Контекст выполнения скрипта
     * @return Результат выполнения действия
     */
    ExecutionResult execute(CodeBlock block, ExecutionContext context);
}