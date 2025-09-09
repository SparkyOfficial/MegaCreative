package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.DataValue;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ArrayList;

public class ListOperationAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // Получаем параметры из блока
            String listName = block.getParameter("list_name").asString();
            String operation = block.getParameter("operation").asString();
            
            // Получаем менеджер переменных
            var variableManager = context.getPlugin().getVariableManager();
            
            // Получаем или создаем список
            DataValue listValue = variableManager.getVariable(listName);
            List<DataValue> list;
            
            if (listValue != null && listValue.isList()) {
                list = listValue.asList();
            } else {
                list = new ArrayList<>();
                variableManager.setVariable(listName, new DataValue(list));
            }
            
            // Выполняем операцию в зависимости от типа
            switch (operation.toLowerCase()) {
                case "add":
                    DataValue valueToAdd = block.getParameter("value");
                    if (valueToAdd != null) {
                        list.add(valueToAdd);
                        variableManager.setVariable(listName, new DataValue(list));
                    }
                    break;
                    
                case "remove":
                    int indexToRemove = block.getParameter("index").asNumber().intValue();
                    if (indexToRemove >= 0 && indexToRemove < list.size()) {
                        list.remove(indexToRemove);
                        variableManager.setVariable(listName, new DataValue(list));
                    }
                    break;
                    
                case "get":
                    int indexToGet = block.getParameter("index").asNumber().intValue();
                    if (indexToGet >= 0 && indexToGet < list.size()) {
                        DataValue result = list.get(indexToGet);
                        String targetVariable = block.getParameter("target_variable").asString();
                        variableManager.setVariable(targetVariable, result);
                    }
                    break;
                    
                case "set":
                    int indexToSet = block.getParameter("index").asNumber().intValue();
                    DataValue valueToSet = block.getParameter("value");
                    if (indexToSet >= 0 && indexToSet < list.size() && valueToSet != null) {
                        list.set(indexToSet, valueToSet);
                        variableManager.setVariable(listName, new DataValue(list));
                    }
                    break;
                    
                case "size":
                    String sizeVariable = block.getParameter("size_variable").asString();
                    variableManager.setVariable(sizeVariable, new DataValue(list.size()));
                    break;
                    
                default:
                    return ExecutionResult.error("Неизвестная операция со списком: " + operation);
            }
            
            return ExecutionResult.success("Операция '" + operation + "' со списком '" + listName + "' выполнена.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при выполнении операции со списком: " + e.getMessage());
        }
    }
}