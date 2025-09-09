package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.FunctionManager;
import org.bukkit.entity.Player;

/**
 * Action for defining custom functions.
 * When executed, it registers the function in the FunctionManager.
 */
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
            
            // Получаем первый блок тела функции
            CodeBlock firstBlock = block.getChildren().get(0);
            
            // Получаем менеджер функций
            FunctionManager functionManager = context.getPlugin().getServiceRegistry().getFunctionManager();
            if (functionManager == null) {
                return ExecutionResult.error("Менеджер функций не доступен.");
            }
            
            // Получаем имя мира
            String worldName = player.getWorld().getName();
            
            // Регистрируем функцию
            functionManager.registerFunction(worldName, functionName, firstBlock);
            
            // Не выполняем дочерние блоки немедленно - они будут выполнены при вызове функции
            return ExecutionResult.success("Функция '" + functionName + "' определена.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при определении функции: " + e.getMessage());
        }
    }
}