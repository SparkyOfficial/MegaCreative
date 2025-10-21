package com.megacreative.coding;

import com.megacreative.coding.BlockType;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

/**
 * Unified interface for script execution engines.
 * Provides a consistent API for executing scripts with support for both
 * synchronous and asynchronous execution modes.
 * 
 * Единый интерфейс для движков выполнения скриптов.
 * Предоставляет согласованный API для выполнения скриптов с поддержкой как
 * синхронного, так и асинхронного режимов выполнения.
 * 
 * @author Андрій Budильников
 */
public interface ScriptEngine {

    /**
     * Executes a script asynchronously.
     *
     * @param script The script to execute
     * @param player The player who triggered the execution (can be null for console/automated)
     * @param trigger The event or condition that triggered execution
     * @return A CompletableFuture that completes when execution finishes
     * 
     * Выполняет скрипт асинхронно.
     *
     * @param script Скрипт для выполнения
     * @param player Игрок, который запустил выполнение (может быть null для консоли/автоматического)
     * @param trigger Событие или условие, которое запустило выполнение
     * @return CompletableFuture, который завершается при окончании выполнения
     */
    CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, String trigger);
    
    /**
     * Executes a single code block asynchronously.
     * This is used for executing individual blocks within a script.
     *
     * @param block The code block to execute
     * @param player The player who triggered the execution (can be null)
     * @param trigger The event or condition that triggered execution
     * @return A CompletableFuture that completes when the block execution finishes
     * 
     * Выполняет один блок кода асинхронно.
     * Используется для выполнения отдельных блоков внутри скрипта.
     *
     * @param block Блок кода для выполнения
     * @param player Игрок, который запустил выполнение (может быть null)
     * @param trigger Событие или условие, которое запустило выполнение
     * @return CompletableFuture, который завершается при окончании выполнения блока
     */
    CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, String trigger);
    
    /**
     * Executes a chain of code blocks starting from a specific block.
     * This is used for executing blocks that trigger other blocks (like WaitAction, RepeatAction).
     *
     * @param startBlock The first block in the chain to execute
     * @param player The player who triggered the execution (can be null)
     * @param trigger The event or condition that triggered execution
     * @return A CompletableFuture that completes when the block chain execution finishes
     * 
     * Выполняет цепочку блоков кода, начиная с определенного блока.
     * Используется для выполнения блоков, которые запускают другие блоки (например, WaitAction, RepeatAction).
     *
     * @param startBlock Первый блок в цепочке для выполнения
     * @param player Игрок, который запустил выполнение (может быть null)
     * @param trigger Событие или условие, которое запустило выполнение
     * @return CompletableFuture, который завершается при окончании выполнения цепочки блоков
     */
    CompletableFuture<ExecutionResult> executeBlockChain(CodeBlock startBlock, Player player, String trigger);

    /**
     * Registers a block action handler.
     *
     * @param type The block type this action handles
     * @param action The action implementation
     * 
     * Регистрирует обработчик действия блока.
     *
     * @param type Тип блока, который обрабатывает это действие
     * @param action Реализация действия
     */
    void registerAction(BlockType type, BlockAction action);

    /**
     * Registers a block condition handler.
     *
     * @param type The block type this condition handles
     * @param condition The condition implementation
     * 
     * Регистрирует обработчик условия блока.
     *
     * @param type Тип блока, который обрабатывает это условие
     * @param condition Реализация условия
     */
    void registerCondition(BlockType type, BlockCondition condition);
    
    /**
     * Gets the block type for a given material and action name.
     * This is used to map configuration-based block types to their enum representations.
     *
     * @param material The block material
     * @param actionName The action name from configuration
     * @return The corresponding BlockType, or null if not found
     * 
     * Получает тип блока для заданного материала и имени действия.
     * Используется для сопоставления типов блоков на основе конфигурации с их перечислениями.
     *
     * @param material Материал блока
     * @param actionName Имя действия из конфигурации
     * @return Соответствующий BlockType или null, если не найден
     */
    BlockType getBlockType(Material material, String actionName);

    /**
     * Gets the variable manager for this engine.
     * @return The VariableManager instance
     * 
     * Получает менеджер переменных для этого движка.
     * @return Экземпляр VariableManager
     */
    VariableManager getVariableManager();

    /**
     * Gets the visual debugger for this engine.
     * @return The VisualDebugger instance
     * 
     * Получает визуальный отладчик для этого движка.
     * @return Экземпляр VisualDebugger
     */
    VisualDebugger getDebugger();

    /**
     * Pauses execution of a running script.
     *
     * @param executionId The ID of the execution to pause
     * @return true if the execution was found and paused
     * 
     * Приостанавливает выполнение запущенного скрипта.
     *
     * @param executionId ID выполнения для приостановки
     * @return true, если выполнение было найдено и приостановлено
     */
    boolean pauseExecution(String executionId);

    /**
     * Resumes a paused script execution.
     *
     * @param executionId The ID of the execution to resume
     * @return true if the execution was found and resumed
     * 
     * Возобновляет приостановленное выполнение скрипта.
     *
     * @param executionId ID выполнения для возобновления
     * @return true, если выполнение было найдено и возобновлено
     */
    boolean resumeExecution(String executionId);

    /**
     * Steps through a single block in a paused script.
     *
     * @param executionId The ID of the execution to step
     * @return true if the step was successful
     * 
     * Выполняет один шаг через один блок в приостановленном скрипте.
     *
     * @param executionId ID выполнения для шага
     * @return true, если шаг был успешным
     */
    boolean stepExecution(String executionId);

    /**
     * Stops a running script execution.
     *
     * @param executionId The ID of the execution to stop
     * @return true if the execution was found and stopped
     * 
     * Останавливает выполнение запущенного скрипта.
     *
     * @param executionId ID выполнения для остановки
     * @return true, если выполнение было найдено и остановлено
     */
    boolean stopExecution(String executionId);
}