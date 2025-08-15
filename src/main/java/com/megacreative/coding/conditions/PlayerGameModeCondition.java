package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerGameModeCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return false;

        // Получаем и разрешаем параметры
        Object rawMode = block.getParameter("mode");

        String modeStr = ParameterResolver.resolve(context, rawMode);

        if (modeStr == null) return false;

        try {
            GameMode requiredMode = GameMode.valueOf(modeStr.toUpperCase());
            return player.getGameMode() == requiredMode;
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cНеизвестный режим игры: " + modeStr);
            return false;
        }
    }
} 