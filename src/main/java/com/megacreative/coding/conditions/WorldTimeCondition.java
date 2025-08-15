package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class WorldTimeCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return false;

        // Получаем и разрешаем параметры
        Object rawTime = block.getParameter("time");
        Object rawOperator = block.getParameter("operator");

        String timeStr = ParameterResolver.resolve(context, rawTime);
        String operatorStr = ParameterResolver.resolve(context, rawOperator);

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