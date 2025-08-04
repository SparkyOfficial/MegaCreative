package com.megacreative.coding.compatibility;

import com.megacreative.coding.actions.IActionBlock;
import com.megacreative.coding.blocks.Block;
import com.megacreative.coding.core.BlockContext;
import com.megacreative.coding.core.BlockType;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Адаптер для совместимости старой системы действий с новой системой блоков.
 */
public class ActionBlockAdapter extends Block {
    private final IActionBlock actionBlock;
    
    public ActionBlockAdapter(String id, IActionBlock actionBlock) {
        super(id, BlockType.ACTION, 
              actionBlock.getActionName(), 
              "Action: " + actionBlock.getActionName(), 
              Material.DIAMOND_BLOCK);
        this.actionBlock = actionBlock;
    }
    
    @Override
    public boolean execute(BlockContext context) {
        try {
            // Создаем CodeBlock с параметрами
            CodeBlock codeBlock = new CodeBlock();
            codeBlock.setAction(actionBlock.getActionName());
            
            // Копируем параметры из контекста
            if (context.getParameters() != null) {
                codeBlock.getParameters().putAll(context.getParameters());
            }
            
            // Создаем ExecutionContext
            ExecutionContext executionContext = new ExecutionContext(
                context.getPlayer(),
                context.getEvent()
            );
            
            // Устанавливаем текущий блок в контекст
            executionContext.setCurrentBlock(codeBlock);
            
            // Выполняем действие
            actionBlock.execute(executionContext, codeBlock);
            return true;
        } catch (Exception e) {
            // Логируем ошибку
            Player player = context.getPlayer();
            if (player != null) {
                player.sendMessage("§cОшибка при выполнении действия: " + e.getMessage());
            }
            getLogger().warning("Ошибка при выполнении действия " + actionBlock.getActionName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
