package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class WorldTimeCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return false;

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawTime = block.getParameter("time");
        DataValue rawOperator = block.getParameter("operator");

        String timeStr = resolver.resolve(context, rawTime).asString();
        String operatorStr = resolver.resolve(context, rawOperator).asString();

        if (timeStr == null) return false;

        try {
            long currentTime = player.getWorld().getTime();
            long targetTime = Long.parseLong(timeStr);

            if (operatorStr == null) {
                // Если оператор не указан, проверяем точное совпадение
                return currentTime == targetTime;
            }

            // Сравниваем время с указанным оператором
            switch (operatorStr.toLowerCase()) {
                case "==":
                case "=":
                    return currentTime == targetTime;
                case "!=":
                case "<>":
                    return currentTime != targetTime;
                case ">":
                    return currentTime > targetTime;
                case ">=":
                    return currentTime >= targetTime;
                case "<":
                    return currentTime < targetTime;
                case "<=":
                    return currentTime <= targetTime;
                default:
                    return false;
            }

        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: время должно быть числом");
            return false;
        }
    }
} 