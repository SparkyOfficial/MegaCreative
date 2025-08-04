package com.megacreative.coding.blocks.conditions;

import com.megacreative.coding.blocks.Block;
import com.megacreative.coding.core.BlockContext;
import com.megacreative.coding.core.BlockType;
import org.bukkit.Material;

/**
 * Базовый класс для всех блоков-условий.
 * Условия возвращают true или false в зависимости от выполнения условия.
 */
public abstract class ConditionBlock extends Block {
    
    protected ConditionBlock(String id, String name, String description, Material icon) {
        super(id, BlockType.CONDITION, name, description, icon);
    }
    
    @Override
    public final boolean execute(BlockContext context) {
        try {
            return checkCondition(context);
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Ошибка при проверке условия " + getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Проверяет условие.
     * 
     * @param context Контекст выполнения
     * @return true, если условие выполнено, иначе false
     */
    public abstract boolean checkCondition(BlockContext context);
}
