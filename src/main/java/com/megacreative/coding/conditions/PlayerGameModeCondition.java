package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a player is in a specific game mode from the new parameter system.
 * This condition returns true if the player is in the specified game mode.
 */
@BlockMeta(id = "playerGameMode", displayName = "§aPlayer Game Mode", type = BlockType.CONDITION)
public class PlayerGameModeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            
            DataValue modeValue = block.getParameter("mode");
            
            
            if (modeValue == null || modeValue.isEmpty()) {
                context.getPlugin().getLogger().warning("PlayerGameModeCondition: 'mode' parameter is missing.");
                return false;
            }
            
            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMode = resolver.resolve(context, modeValue);
            
            String modeName = resolvedMode.asString();
            // Removed redundant null check - static analysis flagged it as always non-null when this method is called
            if (modeName.isEmpty()) {
                context.getPlugin().getLogger().warning("PlayerGameModeCondition: 'mode' parameter is empty.");
                return false;
            }

            
            try {
                GameMode gameMode = GameMode.valueOf(modeName.toUpperCase());
                return player.getGameMode() == gameMode;
            } catch (IllegalArgumentException e) {
                context.getPlugin().getLogger().warning("PlayerGameModeCondition: Invalid game mode '" + modeName + "'.");
                return false;
            }
        } catch (Exception e) {
            
            context.getPlugin().getLogger().warning("Error in PlayerGameModeCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold game mode parameters
     */
    private static class PlayerGameModeParams {
        String modeStr = "";
    }
}