package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.blocks.Block;
import com.megacreative.coding.core.BlockContext;
import com.megacreative.coding.core.BlockType;
import org.bukkit.Material;

/**
 * Базовый класс для всех блоков-действий.
 * Действия выполняют какую-то операцию и могут изменять состояние игры.
 */
public abstract class ActionBlock extends Block {
    
    protected ActionBlock(String id, String name, String description, Material icon) {
        super(id, BlockType.ACTION, name, description, icon);
    }
    
    @Override
    public boolean execute(BlockContext context) {
        try {
            return onExecute(context);
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Ошибка при выполнении действия " + getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Выполняет действие блока.
     * 
     * @param context Контекст выполнения
     * @return true, если выполнение прошло успешно
     */
    protected abstract boolean onExecute(BlockContext context);
}
