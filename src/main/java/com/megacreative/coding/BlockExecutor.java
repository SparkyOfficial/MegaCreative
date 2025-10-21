package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;

/**
 * Interface for executing code blocks
 * This is part of the Strategy pattern implementation for the ScriptEngine
 * 
 * Интерфейс для выполнения блоков кода
 * Это часть реализации паттерна Strategy для ScriptEngine
 * 
 * @author Андрій Budильников
 */
public interface BlockExecutor {
    
    /**
     * Execute a code block
     * @param block The code block to execute
     * @param context The execution context
     * @return The result of the execution
     * 
     * Выполнить блок кода
     * @param block Блок кода для выполнения
     * @param context Контекст выполнения
     * @return Результат выполнения
     */
    ExecutionResult execute(CodeBlock block, ExecutionContext context);
}