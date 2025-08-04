package com.megacreative.coding.util;

import com.megacreative.coding.blocks.Block;
import com.megacreative.coding.blocks.BlockType;
import com.megacreative.coding.blocks.conditions.ConditionBlock;
import com.megacreative.coding.blocks.events.EventBlock;
import com.megacreative.coding.blocks.loops.LoopBlock;
import com.megacreative.coding.core.BlockContext;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилиты для работы с блоками.
 */
public final class BlockUtils {
    
    private BlockUtils() {
        // Приватный конструктор для утилитного класса
    }
    
    /**
     * Проверяет, является ли блок дочерним для другого блока.
     */
    public static boolean isChildOf(Block block, Block potentialParent) {
        if (block == null || potentialParent == null) {
            return false;
        }
        
        Block current = block.getParent();
        while (current != null) {
            if (current.equals(potentialParent)) {
                return true;
            }
            current = current.getParent();
        }
        
        return false;
    }
    
    /**
     * Находит все блоки указанного типа в иерархии.
     */
    public static <T extends Block> List<T> findBlocksOfType(Block root, Class<T> blockType) {
        List<T> result = new ArrayList<>();
        findBlocksOfTypeRecursive(root, blockType, result);
        return result;
    }
    
    private static <T extends Block> void findBlocksOfTypeRecursive(Block block, Class<T> blockType, List<T> result) {
        if (block == null) {
            return;
        }
        
        // Проверяем текущий блок
        if (blockType.isInstance(block)) {
            result.add(blockType.cast(block));
        }
        
        // Рекурсивно проверяем дочерние блоки
        for (Block child : block.getChildren()) {
            findBlocksOfTypeRecursive(child, blockType, result);
        }
        
        // Проверяем следующий блок в цепочке
        findBlocksOfTypeRecursive(block.getNextBlock(), blockType, result);
    }
    
    /**
     * Выполняет действие для всех блоков в иерархии.
     */
    public static void forEachBlock(Block root, BlockVisitor visitor) {
        if (root == null || visitor == null) {
            return;
        }
        
        // Обрабатываем текущий блок
        visitor.visit(root);
        
        // Рекурсивно обрабатываем дочерние блоки
        for (Block child : root.getChildren()) {
            forEachBlock(child, visitor);
        }
        
        // Обрабатываем следующий блок в цепочке
        forEachBlock(root.getNextBlock(), visitor);
    }
    
    /**
     * Создает копию блока и всех его дочерних блоков.
     */
    public static Block deepCopy(Block original) {
        if (original == null) {
            return null;
        }
        
        // Создаем копию блока
        Block copy = original.clone();
        
        // Копируем дочерние блоки
        for (Block child : original.getChildren()) {
            Block childCopy = deepCopy(child);
            if (childCopy != null) {
                copy.addChild(childCopy);
            }
        }
        
        // Копируем следующий блок в цепочке
        if (original.getNextBlock() != null) {
            copy.setNextBlock(deepCopy(original.getNextBlock()));
        }
        
        return copy;
    }
    
    /**
     * Получает отображаемое имя для типа блока.
     */
    public static String getDisplayName(BlockType blockType) {
        if (blockType == null) {
            return "Неизвестный";
        }
        
        switch (blockType) {
            case EVENT: return "Событие";
            case ACTION: return "Действие";
            case CONDITION: return "Условие";
            case LOOP: return "Цикл";
            case FUNCTION: return "Функция";
            case VARIABLE: return "Переменная";
            case VALUE: return "Значение";
            case OPERATOR: return "Оператор";
            default: return blockType.name();
        }
    }
    
    /**
     * Проверяет, может ли блок быть подключен к указанному родительскому блоку.
     */
    public static boolean canConnectTo(Block block, Block parent) {
        if (block == null || parent == null) {
            return false;
        }
        
        // Блоки-события не могут быть дочерними
        if (block instanceof EventBlock) {
            return false;
        }
        
        // Проверяем ограничения по типам блоков
        if (parent instanceof EventBlock) {
            // К событиям можно подключать только определенные типы блоков
            return block instanceof ConditionBlock || block instanceof LoopBlock;
        } else if (parent instanceof ConditionBlock) {
            // Условия могут содержать любые блоки, кроме событий
            return !(block instanceof EventBlock);
        } else if (parent instanceof LoopBlock) {
            // Циклы могут содержать любые блоки, кроме событий
            return !(block instanceof EventBlock);
        }
        
        // По умолчанию разрешаем соединение
        return true;
    }
    
    /**
     * Интерфейс для посетителя блоков.
     */
    @FunctionalInterface
    public interface BlockVisitor {
        /**
         * Вызывается для каждого блока в иерархии.
         * 
         * @param block Текущий блок
         */
        void visit(Block block);
    }
}
