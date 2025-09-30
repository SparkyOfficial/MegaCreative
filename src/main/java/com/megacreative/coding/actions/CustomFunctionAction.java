package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.coding.functions.FunctionDefinition;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
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
            
            // Получаем дополнительные параметры функции
            List<FunctionDefinition.FunctionParameter> parameters = new ArrayList<>();
            
            // Получаем параметры функции из блока (если есть)
            DataValue parametersValue = block.getParameter("parameters");
            if (parametersValue != null && !parametersValue.isEmpty()) {
                // Параметры могут быть в формате "name:type, name2:type2"
                String parametersStr = parametersValue.asString();
                if (parametersStr != null && !parametersStr.isEmpty()) {
                    String[] paramPairs = parametersStr.split(",");
                    for (String paramPair : paramPairs) {
                        String[] parts = paramPair.trim().split(":");
                        if (parts.length >= 1) {
                            String paramName = parts[0].trim();
                            ValueType paramType = ValueType.ANY;
                            String description = "Parameter for function " + functionName;
                            
                            // Если указан тип, используем его
                            if (parts.length >= 2) {
                                try {
                                    paramType = ValueType.valueOf(parts[1].trim().toUpperCase());
                                } catch (IllegalArgumentException e) {
                                    // Используем ANY если тип не распознан
                                }
                            }
                            
                            // Создаем параметр функции
                            FunctionDefinition.FunctionParameter param = new FunctionDefinition.FunctionParameter(
                                paramName,
                                paramType,
                                true, // required
                                null, // no default value
                                description
                            );
                            parameters.add(param);
                        }
                    }
                }
            }
            
            // Получаем тип возвращаемого значения (если есть)
            ValueType returnType = null;
            DataValue returnTypeValue = block.getParameter("return_type");
            if (returnTypeValue != null && !returnTypeValue.isEmpty()) {
                try {
                    returnType = ValueType.valueOf(returnTypeValue.asString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Оставляем null если тип не распознан
                }
            }
            
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
                parameters, // Параметры функции
                functionBlocks,
                returnType, // Тип возвращаемого значения
                FunctionDefinition.FunctionScope.WORLD // Область видимости
            );
            
            // Регистрируем функцию
            boolean registered = functionManager.registerFunction(function);
            
            if (registered) {
                return ExecutionResult.success("Функция '" + functionName + "' определена с " + parameters.size() + " параметрами.");
            } else {
                return ExecutionResult.error("Не удалось зарегистрировать функцию '" + functionName + "'.");
            }

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при определении функции: " + e.getMessage());
        }
    }
}