package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;

/**
 * Условие для проверки, едет ли игрок на каком-либо транспорте.
 */
public class IsRidingCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        if (context.getPlayer() == null) {
            return false;
        }
        
        return context.getPlayer().isInsideVehicle();
    }
}