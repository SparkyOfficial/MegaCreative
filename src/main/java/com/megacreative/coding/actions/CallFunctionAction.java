package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.services.FunctionManager;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Action for calling custom functions.
 * When executed, it finds and executes the registered function.
 */
public class CallFunctionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // Получаем параметры из блока
            String functionName = block.getParameter("function_name").asString();
            
            // Получаем менеджер функций
            FunctionManager functionManager = context.getPlugin().getServiceRegistry().getFunctionManager();
            if (functionManager == null) {
                return ExecutionResult.error("Менеджер функций не доступен.");
            }
            
            // Получаем имя мира
            String worldName = player.getWorld().getName();
            
            // Проверяем, существует ли функция
            if (!functionManager.functionExists(worldName, functionName)) {
                return ExecutionResult.error("Функция '" + functionName + "' не найдена.");
            }
            
            // Получаем первый блок функции
            CodeBlock functionBlock = functionManager.getFunction(worldName, functionName);
            if (functionBlock == null) {
                return ExecutionResult.error("Функция '" + functionName + "' имеет некорректное определение.");
            }
            
            // Создаем новый контекст для выполнения функции с локальной областью видимости
            ExecutionContext functionContext = new ExecutionContext.Builder()
                .plugin(context.getPlugin())
                .player(context.getPlayer())
                .creativeWorld(context.getCreativeWorld())
                .currentBlock(functionBlock)
                .build();
            
            // Получаем движок скриптов
            ScriptEngine scriptEngine = context.getPlugin().getCodingManager().getScriptEngine();
            if (scriptEngine == null) {
                return ExecutionResult.error("Движок скриптов не доступен.");
            }
            
            // Выполняем функцию асинхронно
            CompletableFuture<ExecutionResult> future = scriptEngine.executeScript(
                new com.megacreative.coding.CodeScript("function_" + functionName, true, functionBlock, 
                com.megacreative.coding.CodeScript.ScriptType.FUNCTION), player, "function_call");
            
            // Ждем завершения выполнения (в реальной реализации может потребоваться другой подход)
            ExecutionResult result = future.get();
            
            return ExecutionResult.success("Функция '" + functionName + "' выполнена. Результат: " + result.getMessage());

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при вызове функции: " + e.getMessage());
        }
    }
}