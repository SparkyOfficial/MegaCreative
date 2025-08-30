package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerGameModeCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null || block == null) return false;

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawMode = block.getParameter("mode");
        
        try {
            if (rawMode == null) return false;

            DataValue modeValue = resolver.resolve(context, rawMode);
            String modeStr = modeValue.asString();

            if (modeStr == null || modeStr.isEmpty()) return false;

            GameMode requiredMode = GameMode.valueOf(modeStr.toUpperCase());
            return player.getGameMode() == requiredMode;
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cНеизвестный режим игры: " + (rawMode != null ? rawMode.asString() : "null"));
            return false;
        }
    }
} 