package com.megacreative.coding.actions;

import com.megacreative.coding.ExecutionContext;

@FunctionalInterface
public interface BlockCondition {
    /**
     * Проверяет условие.
     * @param context Контекст выполнения
     * @return true, если условие выполнено, иначе false
     */
    boolean check(ExecutionContext context);
} 