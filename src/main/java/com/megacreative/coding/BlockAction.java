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
    default ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        // Default implementation that returns an error for backward compatibility
        return ExecutionResult.error("Action not implemented. Please implement execute(CodeBlock, ExecutionContext) method.");
    }
    
    /**
     * @deprecated Use {@link #execute(CodeBlock, ExecutionContext)} instead
     */
    @Deprecated
    default void execute(ExecutionContext context) {
        throw new UnsupportedOperationException("This method is deprecated. Implement execute(CodeBlock, ExecutionContext) instead.");
    }
}