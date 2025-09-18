package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Action for calling custom functions.
 * When executed, it finds and executes the registered function with proper parameter and return value handling.
 */
public class CallFunctionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        // Initialize functionName to avoid scope issues
        String functionName = "unknown";
        
        try {
            // Получаем параметры из блока
            functionName = block.getParameter("function_name").asString();
            
            // Получаем менеджер функций
            AdvancedFunctionManager functionManager = context.getPlugin().getServiceRegistry().getAdvancedFunctionManager();
            if (functionManager == null) {
                return ExecutionResult.error("Менеджер функций не доступен.");
            }
            
            // Проверяем, существует ли функция
            if (functionManager.findFunction(functionName, player) == null) {
                return ExecutionResult.error("Функция '" + functionName + "' не найдена.");
            }
            
            // Получаем аргументы функции из блока
            DataValue[] arguments = getFunctionArguments(block, context);
            
            // Выполняем функцию асинхронно
            CompletableFuture<ExecutionResult> future = functionManager.executeFunction(functionName, player, arguments);
            
            // Ждем завершения выполнения
            ExecutionResult result = future.get();
            
            // Обрабатываем возвращаемое значение
            if (result.getReturnValue() != null) {
                // Сохраняем возвращаемое значение в переменную, если указано
                DataValue returnValueVar = block.getParameter("return_variable");
                if (returnValueVar != null && !returnValueVar.isEmpty()) {
                    String varName = returnValueVar.asString();
                    context.setVariable(varName, result.getReturnValue());
                }
                
                return ExecutionResult.success("Функция '" + functionName + "' выполнена. Возвращаемое значение: " + result.getReturnValue());
            }
            
            // Проверяем, была ли функция завершена оператором return
            if (result.isTerminated()) {
                // Если функция завершена return, то прекращаем выполнение текущего скрипта
                ExecutionResult terminatedResult = ExecutionResult.success("Функция '" + functionName + "' завершена оператором return");
                terminatedResult.setTerminated(true);
                return terminatedResult;
            }
            
            return ExecutionResult.success("Функция '" + functionName + "' выполнена успешно.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при вызове функции '" + functionName + "': " + e.getMessage());
        }
    }
    
    /**
     * Получает аргументы функции из блока кода
     */
    private DataValue[] getFunctionArguments(CodeBlock block, ExecutionContext context) {
        // Получаем аргументы из параметров блока
        DataValue argsValue = block.getParameter("arguments");
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