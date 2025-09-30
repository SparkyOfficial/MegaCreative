package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Action for calling custom functions.
 * When executed, it finds and executes the registered function with proper parameter and return value handling.
 */
@BlockMeta(id = "callFunction", displayName = "§aCall Function", type = BlockType.ACTION)
public class CallFunctionAction implements BlockAction {
    private static final String FUNCTION_NAME_PARAM = "function_name";
    private static final String RETURN_VAR_PARAM = "return_variable";
    private static final String ARGUMENTS_PARAM = "arguments";
    private static final String FUNCTION_NOT_FOUND_MSG = "Функция '%s' не найдена.";
    private static final String FUNCTION_EXECUTED_MSG = "Функция '%s' выполнена успешно.";
    private static final String FUNCTION_RETURN_MSG = "Функция '%s' выполнена. Возвращаемое значение: %s";
    private static final String FUNCTION_TERMINATED_MSG = "Функция '%s' завершена оператором return";
    private static final String FUNCTION_CALL_ERROR_MSG = "Ошибка при вызове функции '%s': %s";
    private static final String PLAYER_NOT_FOUND_MSG = "Игрок не найден.";
    private static final String FUNCTION_MANAGER_UNAVAILABLE_MSG = "Менеджер функций не доступен.";

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error(PLAYER_NOT_FOUND_MSG);
        }

        try {
            // Получаем параметры из блока (final to use in lambda)
            final String functionName = block.getParameter(FUNCTION_NAME_PARAM).asString();
            
            // Получаем менеджер функций
            AdvancedFunctionManager functionManager = context.getPlugin().getServiceRegistry().getAdvancedFunctionManager();
            if (functionManager == null) {
                return ExecutionResult.error(FUNCTION_MANAGER_UNAVAILABLE_MSG);
            }
            
            // Проверяем, существует ли функция
            if (functionManager.findFunction(functionName, player) == null) {
                return ExecutionResult.error(String.format(FUNCTION_NOT_FOUND_MSG, functionName));
            }
            
            // Получаем аргументы функции из блока
            DataValue[] arguments = getFunctionArguments(block);
            
            // Выполняем функцию асинхронно
            CompletableFuture<ExecutionResult> future = functionManager.executeFunction(functionName, player, arguments);
            
            // Handle the result asynchronously without blocking the main thread
            future.thenAccept(result -> {
                // This code runs when the function execution completes
                
                // Обрабатываем возвращаемое значение
                if (result.getReturnValue() != null) {
                    // Сохраняем возвращаемое значение в переменную, если указано
                    DataValue returnValueVar = block.getParameter(RETURN_VAR_PARAM);
                    if (returnValueVar != null && !returnValueVar.isEmpty()) {
                        String varName = returnValueVar.asString();
                        context.setVariable(varName, result.getReturnValue());
                    }
                    
                    // Send success message to player
                    player.sendMessage(String.format(FUNCTION_RETURN_MSG, functionName, result.getReturnValue()));
                } else {
                    // Send execution message to player
                    player.sendMessage(String.format(FUNCTION_EXECUTED_MSG, functionName));
                }
                
                // Проверяем, была ли функция завершена оператором return
                if (result.isTerminated()) {
                    // Handle termination - in a real implementation, this would need to be handled
                    // by the script engine to stop the execution chain
                    player.sendMessage(String.format(FUNCTION_TERMINATED_MSG, functionName));
                }
            }).exceptionally(throwable -> {
                // Handle any exceptions that occurred during function execution
                player.sendMessage(String.format(FUNCTION_CALL_ERROR_MSG, functionName, throwable.getMessage()));
                return null;
            });
            
            // Return immediately without blocking - the function will execute asynchronously
            return ExecutionResult.success("Function call initiated for: " + functionName);

        } catch (Exception e) {
            return ExecutionResult.error(
                String.format(FUNCTION_CALL_ERROR_MSG, "unknown", e.getMessage())
            );
        }
    }
    
    /**
     * Получает аргументы функции из блока кода
     */
    private DataValue[] getFunctionArguments(CodeBlock block) {
        // Получаем аргументы из параметров блока
        DataValue argsValue = block.getParameter(ARGUMENTS_PARAM);
        if (argsValue != null && argsValue.getValue() instanceof Object[]) {
            Object[] argsArray = (Object[]) argsValue.getValue();
            DataValue[] arguments = new DataValue[argsArray.length];
            for (int i = 0; i < argsArray.length; i++) {
                arguments[i] = DataValue.fromObject(argsArray[i]);
            }
            return arguments;
        }
        
        // По умолчанию возвращаем пустой массив
        return new DataValue[0];
    }
}