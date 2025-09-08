package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Условие для проверки игрового режима игрока.
 */
public class PlayerGameModeCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawGameMode = block.getParameter("gameMode");

        if (rawGameMode == null) {
            context.getPlugin().getLogger().warning("Game mode not specified in PlayerGameModeCondition");
            return false;
        }

        DataValue gameModeValue = resolver.resolve(context, rawGameMode);
        String gameModeName = gameModeValue.asString();

        try {
            GameMode requiredGameMode = GameMode.valueOf(gameModeName.toUpperCase());
            return player.getGameMode() == requiredGameMode;
        } catch (IllegalArgumentException e) {
            context.getPlugin().getLogger().warning("Invalid game mode in PlayerGameModeCondition: " + gameModeName);
            return false;
        }
    }
}