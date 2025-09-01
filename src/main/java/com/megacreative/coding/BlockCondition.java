package com.megacreative.coding;

/**
 * Интерфейс для условий блоков кода.
 * Реализуется классами, которые проверяют различные условия во время выполнения скрипта.
 */
public interface BlockCondition {
    
    /**
     * Вычисляет результат условия.
     * @param block Блок кода для проверки условия
     * @param context Контекст выполнения скрипта
     * @return true если условие выполнено, false в противном случае
     */
    default boolean evaluate(CodeBlock block, ExecutionContext context) {
        // Default implementation for backward compatibility
        try {
            context.setCurrentBlock(block);
            return evaluate(context);
        } catch (Exception e) {
            context.getLogger().severe("Error evaluating condition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * @deprecated Use {@link #evaluate(CodeBlock, ExecutionContext)} instead
     */
    @Deprecated
    default boolean evaluate(ExecutionContext context) {
        throw new UnsupportedOperationException("This method is deprecated. Implement evaluate(CodeBlock, ExecutionContext) instead.");
    }
}