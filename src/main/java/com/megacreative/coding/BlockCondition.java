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
    boolean evaluate(CodeBlock block, ExecutionContext context);
}