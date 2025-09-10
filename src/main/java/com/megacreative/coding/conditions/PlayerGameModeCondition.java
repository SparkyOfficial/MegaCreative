package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Condition for checking a player's game mode.
 * This condition returns true if the player is in the specified game mode.
 */
public class PlayerGameModeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get the game mode parameter from the block
            DataValue modeValue = block.getParameter("mode");
            if (modeValue == null) {
                return false;
            }

            // Resolve any placeholders in the game mode
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMode = resolver.resolve(context, modeValue);
            
            // Parse game mode parameter
            String modeName = resolvedMode.asString();
            if (modeName == null || modeName.isEmpty()) {
                return false;
            }

            // Check if player is in the specified game mode
            try {
                GameMode gameMode = GameMode.valueOf(modeName.toUpperCase());
                return player.getGameMode() == gameMode;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}