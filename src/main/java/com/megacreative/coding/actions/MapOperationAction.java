package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.values.types.MapValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.values.types.TextValue;
import com.megacreative.coding.variables.IVariableManager;
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
            DataValue mapValue = variableManager.getVariable(mapName, IVariableManager.VariableScope.PLAYER, player.getName());
            Map<String, DataValue> map;
            
            if (mapValue != null && mapValue.getType() == ValueType.DICTIONARY) {
                // Convert to Map<String, DataValue>
                map = ((com.megacreative.coding.values.types.MapValue) mapValue).getValue();
            } else {
                map = new HashMap<>();
                variableManager.setVariable(mapName, new MapValue(map), IVariableManager.VariableScope.PLAYER, player.getName());
            }
            
            // Выполняем операцию в зависимости от типа
            switch (operation.toLowerCase()) {
                case "put":
                    String keyToPut = block.getParameter("key").asString();
                    DataValue valueToPut = block.getParameter("value");
                    if (keyToPut != null && valueToPut != null) {
                        map.put(keyToPut, valueToPut);
                        variableManager.setVariable(mapName, new MapValue(map), IVariableManager.VariableScope.PLAYER, player.getName());
                    }
                    break;
                    
                case "get":
                    String keyToGet = block.getParameter("key").asString();
                    if (keyToGet != null && map.containsKey(keyToGet)) {
                        DataValue result = map.get(keyToGet);
                        String targetVariable = block.getParameter("target_variable").asString();
                        variableManager.setVariable(targetVariable, result, IVariableManager.VariableScope.PLAYER, player.getName());
                    }
                    break;
                    
                case "remove":
                    String keyToRemove = block.getParameter("key").asString();
                    if (keyToRemove != null && map.containsKey(keyToRemove)) {
                        map.remove(keyToRemove);
                        variableManager.setVariable(mapName, new MapValue(map), IVariableManager.VariableScope.PLAYER, player.getName());
                    }
                    break;
                    
                case "keys":
                    // Возвращаем список всех ключей
                    List<DataValue> keys = new ArrayList<>();
                    for (String k : map.keySet()) {
                        keys.add(new TextValue(k));
                    }
                    String keysVariable = block.getParameter("keys_variable").asString();
                    variableManager.setVariable(keysVariable, new ListValue(keys), IVariableManager.VariableScope.PLAYER, player.getName());
                    break;
                    
                case "values":
                    // Возвращаем список всех значений
                    List<DataValue> values = new ArrayList<>(map.values());
                    String valuesVariable = block.getParameter("values_variable").asString();
                    variableManager.setVariable(valuesVariable, new ListValue(values), IVariableManager.VariableScope.PLAYER, player.getName());
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