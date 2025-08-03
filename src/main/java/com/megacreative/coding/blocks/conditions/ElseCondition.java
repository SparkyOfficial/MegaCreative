package com.megacreative.coding.blocks.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.ExecutionContext;

/**
 * Блок ELSE - выполняется, когда предыдущее условие IF ложно.
 * Этот блок всегда возвращает true, так как он выполняется только
 * когда система решает, что нужно выполнить ELSE-ветку.
 */
public class ElseCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(ExecutionContext context) {
        // ELSE всегда возвращает true, так как он выполняется
        // только когда система уже решила выполнить ELSE-ветку
        return true;
    }
} 