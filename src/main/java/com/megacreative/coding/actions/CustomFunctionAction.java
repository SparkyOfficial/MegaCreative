package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.coding.functions.FunctionDefinition;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Action for defining custom functions.
 * When executed, it registers the function in the AdvancedFunctionManager.
 */
@BlockMeta(id = "customFunction", displayName = "§aCustom Function", type = BlockType.ACTION)
public class CustomFunctionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // Получаем параметры из блока
            String functionName = block.getParameter("function_name").asString();
            
            // Проверяем, есть ли дочерние блоки (тело функции)
            if (block.getChildren().isEmpty()) {
                return ExecutionResult.error("У функции '" + functionName + "' нет тела (дочерних блоков).");
            }
            
            // Получаем блоки тела функции
            List<CodeBlock> functionBlocks = new ArrayList<>(block.getChildren());
            
            // Получаем менеджер функций
            AdvancedFunctionManager functionManager = context.getPlugin().getServiceRegistry().getAdvancedFunctionManager();
            if (functionManager == null) {
                return ExecutionResult.error("Менеджер функций не доступен.");
            }
            
            // Создаем определение функции
            FunctionDefinition function = new FunctionDefinition(
                functionName,
                "Пользовательская функция: " + functionName,
                player,
                new ArrayList<>(), // Параметры (пока пустой список)
                functionBlocks,
                null, // Тип возвращаемого значения (пока null)
                FunctionDefinition.FunctionScope.WORLD // Область видимости
            );
            
            // Регистрируем функцию
            boolean registered = functionManager.registerFunction(function);
            
            if (registered) {
                return ExecutionResult.success("Функция '" + functionName + "' определена.");
            } else {
                return ExecutionResult.error("Не удалось зарегистрировать функцию '" + functionName + "'.");
            }

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при определении функции: " + e.getMessage());
        }
    }
}