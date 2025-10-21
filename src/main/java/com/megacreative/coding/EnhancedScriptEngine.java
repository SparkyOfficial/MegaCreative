package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.executors.AdvancedExecutionEngine;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * 🎆 Enhanced Script Engine Interface with Reference System-Style Execution Modes
 * 
 * Extends the basic ScriptEngine with advanced execution capabilities:
 * - Multiple execution modes (sync, async, delayed, batch, prioritized)
 * - Priority-based execution scheduling
 * - Performance monitoring and analytics
 * - Execution context isolation
 * - Resource management and throttling
 * 
 * 🎆 Расширенный интерфейс движка скриптов с режимами выполнения в стиле Reference System
 * 
 * Расширяет базовый ScriptEngine с расширенными возможностями выполнения:
 * - Множественные режимы выполнения (синхронный, асинхронный, с задержкой, пакетный, с приоритетом)
 * - Планирование выполнения на основе приоритетов
 * - Мониторинг производительности и аналитика
 * - Изоляция контекста выполнения
 * - Управление ресурсами и регулирование
 * 
 * @author Андрій Budильников
 */
public interface EnhancedScriptEngine extends ScriptEngine {
    
    /**
     * Execute a script with specified execution mode and priority
     * 
     * @param script The script to execute
     * @param player The player context
     * @param mode The execution mode
     * @param priority The execution priority
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     * 
     * Выполнить скрипт с указанным режимом выполнения и приоритетом
     * 
     * @param script Скрипт для выполнения
     * @param player Контекст игрока
     * @param mode Режим выполнения
     * @param priority Приоритет выполнения
     * @param trigger Триггер, вызвавший выполнение
     * @return CompletableFuture с результатом выполнения
     */
    CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, 
                                                   AdvancedExecutionEngine.ExecutionMode mode, 
                                                   AdvancedExecutionEngine.Priority priority, 
                                                   String trigger);
    
    /**
     * Execute a code block with specified execution mode and priority
     * 
     * @param block The block to execute
     * @param player The player context
     * @param mode The execution mode
     * @param priority The execution priority
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     * 
     * Выполнить блок кода с указанным режимом выполнения и приоритетом
     * 
     * @param block Блок для выполнения
     * @param player Контекст игрока
     * @param mode Режим выполнения
     * @param priority Приоритет выполнения
     * @param trigger Триггер, вызвавший выполнение
     * @return CompletableFuture с результатом выполнения
     */
    CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, 
                                                  AdvancedExecutionEngine.ExecutionMode mode, 
                                                  AdvancedExecutionEngine.Priority priority, 
                                                  String trigger);
    
    /**
     * Execute a script with delayed execution
     * 
     * @param script The script to execute
     * @param player The player context
     * @param delayTicks The delay in server ticks
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     * 
     * Выполнить скрипт с отложенным выполнением
     * 
     * @param script Скрипт для выполнения
     * @param player Контекст игрока
     * @param delayTicks Задержка в тиках сервера
     * @param trigger Триггер, вызвавший выполнение
     * @return CompletableFuture с результатом выполнения
     */
    CompletableFuture<ExecutionResult> executeScriptDelayed(CodeScript script, Player player, 
                                                           long delayTicks, String trigger);
    
    /**
     * Execute multiple scripts in batch mode
     * 
     * @param scripts Array of scripts to execute
     * @param player The player context
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with batch execution result
     * 
     * Выполнить несколько скриптов в пакетном режиме
     * 
     * @param scripts Массив скриптов для выполнения
     * @param player Контекст игрока
     * @param trigger Триггер, вызвавший выполнение
     * @return CompletableFuture с результатом пакетного выполнения
     */
    CompletableFuture<ExecutionResult[]> executeScriptsBatch(CodeScript[] scripts, Player player, String trigger);
    
    /**
     * Cancel all executions for a specific player
     * 
     * @param player The player whose executions to cancel
     * 
     * Отменить все выполнения для определенного игрока
     * 
     * @param player Игрок, чьи выполнения нужно отменить
     */
    void cancelPlayerExecutions(Player player);
    
    /**
     * Get execution statistics and performance metrics
     * 
     * @return Execution statistics
     * 
     * Получить статистику выполнения и метрики производительности
     * 
     * @return Статистика выполнения
     */
    AdvancedExecutionEngine.ExecutionStatistics getExecutionStatistics();
    
    /**
     * Set the maximum execution time for scripts
     * 
     * @param maxTimeMs Maximum execution time in milliseconds
     * 
     * Установить максимальное время выполнения для скриптов
     * 
     * @param maxTimeMs Максимальное время выполнения в миллисекундах
     */
    void setMaxExecutionTime(long maxTimeMs);
    
    /**
     * Set the maximum instructions per tick to prevent lag
     * 
     * @param maxInstructions Maximum instructions per server tick
     * 
     * Установить максимальное количество инструкций на тик для предотвращения лагов
     * 
     * @param maxInstructions Максимальное количество инструкций на тик сервера
     */
    void setMaxInstructionsPerTick(int maxInstructions);
    
    /**
     * Check if the engine is currently overloaded
     * 
     * @return true if the engine is under heavy load
     * 
     * Проверить, перегружен ли движок в данный момент
     * 
     * @return true, если движок находится под большой нагрузкой
     */
    boolean isOverloaded();
    
    /**
     * Get the current throughput (executions per second)
     * 
     * @return Current execution throughput
     * 
     * Получить текущую пропускную способность (выполнения в секунду)
     * 
     * @return Текущая пропускная способность выполнения
     */
    double getCurrentThroughput();
    
    /**
     * Prioritize execution for critical operations
     * 
     * @param script The script to prioritize
     * @param player The player context
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     * 
     * Приоритизировать выполнение для критических операций
     * 
     * @param script Скрипт для приоритизации
     * @param player Контекст игрока
     * @param trigger Триггер, вызвавший выполнение
     * @return CompletableFuture с результатом выполнения
     */
    default CompletableFuture<ExecutionResult> executeScriptCritical(CodeScript script, Player player, String trigger) {
        return executeScript(script, player, AdvancedExecutionEngine.ExecutionMode.SYNCHRONOUS, 
                           AdvancedExecutionEngine.Priority.CRITICAL, trigger);
    }
    
    /**
     * Execute script asynchronously for non-critical operations
     * 
     * @param script The script to execute
     * @param player The player context
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     * 
     * Выполнить скрипт асинхронно для некритических операций
     * 
     * @param script Скрипт для выполнения
     * @param player Контекст игрока
     * @param trigger Триггер, вызвавший выполнение
     * @return CompletableFuture с результатом выполнения
     */
    default CompletableFuture<ExecutionResult> executeScriptAsync(CodeScript script, Player player, String trigger) {
        return executeScript(script, player, AdvancedExecutionEngine.ExecutionMode.ASYNCHRONOUS, 
                           AdvancedExecutionEngine.Priority.NORMAL, trigger);
    }
    
    /**
     * Execute script with low priority for background operations
     * 
     * @param script The script to execute
     * @param player The player context
     * @param trigger The trigger that caused execution
     * @return CompletableFuture with execution result
     * 
     * Выполнить скрипт с низким приоритетом для фоновых операций
     * 
     * @param script Скрипт для выполнения
     * @param player Контекст игрока
     * @param trigger Триггер, вызвавший выполнение
     * @return CompletableFuture с результатом выполнения
     */
    default CompletableFuture<ExecutionResult> executeScriptBackground(CodeScript script, Player player, String trigger) {
        return executeScript(script, player, AdvancedExecutionEngine.ExecutionMode.ASYNCHRONOUS, 
                           AdvancedExecutionEngine.Priority.LOW, trigger);
    }
}