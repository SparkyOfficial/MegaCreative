package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class SubVarAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);

        // Получаем и разрешаем параметры
        DataValue rawVarName = block.getParameter("var");
        DataValue rawValue = block.getParameter("value");

        if (rawVarName == null || rawValue == null) return;

        String varName = resolver.resolve(context, rawVarName).asString();
        String valueStr = resolver.resolve(context, rawValue).asString();

        if (varName == null || valueStr == null) return;

        try {
            // Получаем текущее значение переменной через VariableManager для типобезопасности
            DataValue currentValueObj = variableManager.getVariable(varName, context.getScriptId(), context.getWorldId());
            double currentNum = 0.0;
            
            if (currentValueObj != null && !currentValueObj.isEmpty()) {
                try {
                    currentNum = currentValueObj.asNumber().doubleValue();
                } catch (NumberFormatException e) {
                    // Если не число, начинаем с 0
                }
            }
            
            // Вычитаем новое значение
            double subValue = Double.parseDouble(valueStr);
            double result = currentNum - subValue;
            
            // Сохраняем результат через VariableManager
            variableManager.setVariable(varName, DataValue.fromObject(result), context.getScriptId(), context.getWorldId());
            player.sendMessage("§a✓ Переменная '" + varName + "' уменьшена на " + subValue + " = " + result);
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: значение должно быть числом");
        }
    }
} 