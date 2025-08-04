package com.megacreative.coding.compatibility;

import com.megacreative.coding.actions.IActionBlock;
import com.megacreative.coding.blocks.BlockRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Реестр для управления действиями и их регистрации в системе блоков.
 */
public class ActionRegistry {
    private final Map<String, IActionBlock> actions = new HashMap<>();
    private final BlockRegistry blockRegistry;
    
    public ActionRegistry(BlockRegistry blockRegistry) {
        this.blockRegistry = blockRegistry;
    }
    
    /**
     * Регистрирует новое действие в системе.
     * 
     * @param action Действие для регистрации
     */
    public void registerAction(IActionBlock action) {
        String actionName = action.getActionName();
        if (actions.containsKey(actionName)) {
            throw new IllegalArgumentException("Действие уже зарегистрировано: " + actionName);
        }
        
        actions.put(actionName, action);
        
        // Регистрируем адаптер в BlockRegistry
        String blockId = "action_" + actionName.toLowerCase();
        blockRegistry.registerBlock(blockId, 
            () -> new ActionBlockAdapter(blockId, action));
    }
    
    /**
     * Получает действие по имени.
     * 
     * @param actionName Имя действия
     * @return Действие или null, если не найдено
     */
    public IActionBlock getAction(String actionName) {
        return actions.get(actionName);
    }
    
    /**
     * Проверяет, зарегистрировано ли действие.
     * 
     * @param actionName Имя действия
     * @return true, если действие зарегистрировано
     */
    public boolean hasAction(String actionName) {
        return actions.containsKey(actionName);
    }
    
    /**
     * Возвращает количество зарегистрированных действий.
     * 
     * @return Количество действий
     */
    public int getActionCount() {
        return actions.size();
    }
}
