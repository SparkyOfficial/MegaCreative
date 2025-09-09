package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.DataValue;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class MapOperationAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // Получаем параметры из блока
            String mapName = block.getParameter("map_name").asString();
            String operation = block.getParameter("operation").asString();
            
            // Получаем менеджер переменных
            var variableManager = context.getPlugin().getVariableManager();
            
            // Получаем или создаем карту
            DataValue mapValue = variableManager.getVariable(mapName);
            Map<String, DataValue> map;
            
            if (mapValue != null && mapValue.isMap()) {
                map = mapValue.asMap();
            } else {
                map = new HashMap<>();
                variableManager.setVariable(mapName, new DataValue(map));
            }
            
            // Выполняем операцию в зависимости от типа
            switch (operation.toLowerCase()) {
                case "put":
                    String keyToPut = block.getParameter("key").asString();
                    DataValue valueToPut = block.getParameter("value");
                    if (keyToPut != null && valueToPut != null) {
                        map.put(keyToPut, valueToPut);
                        variableManager.setVariable(mapName, new DataValue(map));
                    }
                    break;
                    
                case "get":
                    String keyToGet = block.getParameter("key").asString();
                    if (keyToGet != null && map.containsKey(keyToGet)) {
                        DataValue result = map.get(keyToGet);
                        String targetVariable = block.getParameter("target_variable").asString();
                        variableManager.setVariable(targetVariable, result);
                    }
                    break;
                    
                case "remove":
                    String keyToRemove = block.getParameter("key").asString();
                    if (keyToRemove != null && map.containsKey(keyToRemove)) {
                        map.remove(keyToRemove);
                        variableManager.setVariable(mapName, new DataValue(map));
                    }
                    break;
                    
                case "keys":
                    // Возвращаем список всех ключей
                    List<DataValue> keys = new ArrayList<>();
                    for (String k : map.keySet()) {
                        keys.add(new DataValue(k));
                    }
                    String keysVariable = block.getParameter("keys_variable").asString();
                    variableManager.setVariable(keysVariable, new DataValue(keys));
                    break;
                    
                case "values":
                    // Возвращаем список всех значений
                    List<DataValue> values = new ArrayList<>(map.values());
                    String valuesVariable = block.getParameter("values_variable").asString();
                    variableManager.setVariable(valuesVariable, new DataValue(values));
                    break;
                    
                default:
                    return ExecutionResult.error("Неизвестная операция с картой: " + operation);
            }
            
            return ExecutionResult.success("Операция '" + operation + "' с картой '" + mapName + "' выполнена.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при выполнении операции с картой: " + e.getMessage());
        }
    }
}