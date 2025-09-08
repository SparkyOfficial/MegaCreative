package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.MegaCreative;
import com.megacreative.managers.FunctionManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Действие для вызова функции с параметрами.
 * Поддерживает параметры из виртуального инвентаря.
 * 
 * Пример использования:
 * onInteract -> callFunction("teleportToSpawn", [компас]) -> sendMessage("Телепортация...")
 */
public class CallFunctionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player is null");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем имя функции из параметра
        DataValue functionNameValue = block.getParameter("functionName");
        if (functionNameValue == null) {
            player.sendMessage("§cОшибка: не указано имя функции!");
            return ExecutionResult.error("Function name not specified");
        }
        
        String functionName = resolver.resolve(context, functionNameValue).asString();
        if (functionName == null || functionName.isEmpty()) {
            player.sendMessage("§cОшибка: не указано имя функции!");
            return ExecutionResult.error("Function name is empty");
        }

        // Получаем параметры из виртуального инвентаря
        Map<String, DataValue> functionParams = new HashMap<>();
        
        // Добавляем стандартные параметры
        for (Map.Entry<String, DataValue> entry : block.getParameters().entrySet()) {
            if (!entry.getKey().equals("functionName")) {
                functionParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Получаем параметры из виртуального инвентаря
        for (int i = 0; i < 27; i++) {
            ItemStack item = block.getConfigItem(i);
            if (item != null) {
                functionParams.put("param_" + i, DataValue.fromObject(item.getType().name()));
                functionParams.put("param_" + i + "_amount", DataValue.fromObject(item.getAmount()));
            }
        }

        // Вызываем функцию
        MegaCreative plugin = context.getPlugin();
        if (plugin != null) {
            // Получаем FunctionManager
            FunctionManager functionManager = plugin.getServiceRegistry().getFunctionManager();
            if (functionManager == null) {
                player.sendMessage("§cОшибка: FunctionManager недоступен");
                return ExecutionResult.error("FunctionManager not available");
            }
            
            // Ищем функцию в текущем мире через FunctionManager
            var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            CodeScript function = null;
            if (creativeWorld != null) {
                function = functionManager.getFunction(creativeWorld, functionName);
            }
            
            if (function != null) {
                // Получаем ScriptEngine из ServiceRegistry
                ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
                if (scriptEngine != null) {
                    CompletableFuture<ExecutionResult> future = scriptEngine.executeScript(function, player, "function_call");
                    try {
                        ExecutionResult result = future.get(); // Ждем завершения выполнения
                        if (result != null && !result.isSuccess()) {
                            player.sendMessage("§cОшибка в функции '" + functionName + "': " + result.getMessage());
                            return ExecutionResult.error("Function execution failed: " + result.getMessage());
                        } else {
                            player.sendMessage("§a✓ Функция '" + functionName + "' выполнена!");
                            return ExecutionResult.success("Function '" + functionName + "' executed successfully");
                        }
                    } catch (Exception e) {
                        player.sendMessage("§cОшибка при выполнении функции '" + functionName + "': " + e.getMessage());
                        return ExecutionResult.error("Error executing function: " + e.getMessage());
                    }
                } else {
                    player.sendMessage("§cОшибка: не удалось получить ScriptEngine");
                    return ExecutionResult.error("ScriptEngine not available");
                }
            } else {
                player.sendMessage("§cОшибка: функция '" + functionName + "' не найдена!");
                return ExecutionResult.error("Function '" + functionName + "' not found");
            }
        }
        
        return ExecutionResult.success();
    }
}