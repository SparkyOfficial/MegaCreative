package com.megacreative.coding.core;

import com.megacreative.coding.blocks.*;
import com.megacreative.coding.blocks.conditions.ConditionBlock;
import com.megacreative.coding.blocks.events.EventBlock;
import com.megacreative.coding.blocks.loops.LoopBlock;

import java.util.*;

/**
 * Класс для валидации скриптов перед выполнением.
 */
public class ScriptValidator {
    private final BlockRegistry blockRegistry;
    
    // Ограничения
    private final int maxBlockDepth = 100; // Максимальная глубина вложенности блоков
    private final int maxBlocks = 1000; // Максимальное общее количество блоков
    private final int maxStringLength = 1000; // Максимальная длина строковых параметров
    
    public ScriptValidator(BlockRegistry blockRegistry) {
        this.blockRegistry = blockRegistry;
    }
    
    /**
     * Проверяет корректность скрипта.
     * 
     * @param rootBlock Корневой блок скрипта
     * @return Результат валидации
     */
    public ValidationResult validate(Block rootBlock) {
        if (rootBlock == null) {
            return ValidationResult.error("Корневой блок не может быть null");
        }
        
        // Проверяем, что корневой блок является блоком-событием
        if (!(rootBlock instanceof EventBlock)) {
            return ValidationResult.error("Корневой блок должен быть блоком-событием");
        }
        
        Set<Block> visited = new HashSet<>();
        Deque<Block> stack = new ArrayDeque<>();
        stack.push(rootBlock);
        
        int blockCount = 0;
        int maxDepth = 0;
        
        // Обходим граф блоков в глубину
        while (!stack.isEmpty()) {
            Block current = stack.pop();
            
            // Проверяем максимальную глубину
            if (stack.size() > maxDepth) {
                maxDepth = stack.size();
                if (maxDepth > maxBlockDepth) {
                    return ValidationResult.error("Превышена максимальная глубина вложенности блоков: " + maxDepth);
                }
            }
            
            // Проверяем общее количество блоков
            blockCount++;
            if (blockCount > maxBlocks) {
                return ValidationResult.error("Превышено максимальное количество блоков: " + blockCount);
            }
            
            // Проверяем, что блок зарегистрирован
            if (!blockRegistry.isBlockRegistered(current.getClass())) {
                return ValidationResult.error("Неизвестный тип блока: " + current.getClass().getName());
            }
            
            // Проверяем параметры блока
            ValidationResult paramsCheck = validateBlockParameters(current);
            if (!paramsCheck.isValid()) {
                return paramsCheck;
            }
            
            // Проверяем специфичные для типов блоков ограничения
            ValidationResult typeCheck = validateBlockTypeSpecific(current);
            if (!typeCheck.isValid()) {
                return typeCheck;
            }
            
            // Добавляем дочерние блоки в стек
            if (current.hasChildren()) {
                for (Block child : current.getChildren()) {
                    if (child != null) {
                        // Проверка на циклические ссылки
                        if (visited.contains(child)) {
                            return ValidationResult.error("Обнаружена циклическая ссылка в блоке: " + current.getId());
                        }
                        stack.push(child);
                    }
                }
            }
            
            // Добавляем следующий блок в стек, если он есть
            if (current.getNextBlock() != null) {
                if (visited.contains(current.getNextBlock())) {
                    return ValidationResult.error("Обнаружена циклическая ссылка в блоке: " + current.getId());
                }
                stack.push(current.getNextBlock());
            }
            
            visited.add(current);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Проверяет параметры блока на корректность.
     */
    private ValidationResult validateBlockParameters(Block block) {
        // Проверяем строковые параметры на длину
        for (Object value : block.getParameters().values()) {
            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.length() > maxStringLength) {
                    return ValidationResult.error("Превышена максимальная длина строкового параметра в блоке: " + block.getId());
                }
            }
        }
        
        // Проверяем обязательные параметры
        for (String requiredParam : block.getRequiredParameters()) {
            if (!block.getParameters().containsKey(requiredParam)) {
                return ValidationResult.error("Отсутствует обязательный параметр '" + requiredParam + "' в блоке: " + block.getId());
            }
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Проверяет специфичные для типов блоков ограничения.
     */
    private ValidationResult validateBlockTypeSpecific(Block block) {
        if (block instanceof ConditionBlock) {
            // Условия должны иметь хотя бы один дочерний блок
            if (!block.hasChildren()) {
                return ValidationResult.error("Условный блок должен содержать хотя бы один дочерний блок: " + block.getId());
            }
        } else if (block instanceof LoopBlock) {
            // Циклы должны иметь хотя бы один дочерний блок
            if (!block.hasChildren()) {
                return ValidationResult.error("Цикл должен содержать хотя бы один дочерний блок: " + block.getId());
            }
            
            // Проверяем параметры цикла
            LoopBlock loop = (LoopBlock) block;
            if (loop.getMaxIterations() <= 0) {
                return ValidationResult.error("Количество итераций цикла должно быть больше 0: " + block.getId());
            }
        } else if (block instanceof EventBlock) {
            // Блоки-события не должны иметь родительских блоков
            if (block.getParent() != null) {
                return ValidationResult.error("Блок-событие не может быть дочерним блоком: " + block.getId());
            }
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Результат валидации.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
