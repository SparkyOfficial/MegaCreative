package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class IfVarEqualsCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null || block == null || variableManager == null) return false;

        ParameterResolver resolver = new ParameterResolver(variableManager);

        try {
            // Получаем и разрешаем параметры
            DataValue rawVarName = block.getParameter("variable");
            DataValue rawValue = block.getParameter("value");

            if (rawVarName == null || rawValue == null) return false;

            DataValue varNameValue = resolver.resolve(context, rawVarName);
            DataValue expectedValue = resolver.resolve(context, rawValue);

            String varName = varNameValue.asString();
            if (varName == null || varName.isEmpty()) return false;

            // Получаем значение переменной через VariableManager для типобезопасности
            DataValue variableValue = variableManager.getVariable(varName, context.getScriptId(), context.getWorldId());
            
            if (variableValue == null || variableValue.isEmpty()) return false;

            // Сравниваем значения через DataValue систему
            return variableValue.asString().equals(expectedValue.asString());

        } catch (Exception e) {
            player.sendMessage("§cОшибка проверки ifVarEquals: " + e.getMessage());
            return false;
        }
    }
} 