package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Action for setting a player's game mode.
 * This action changes the player's game mode based on the parameter.
 */
public class SetGameModeAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the game mode parameter from the block
            DataValue modeValue = block.getParameter("mode");
            if (modeValue == null) {
                return ExecutionResult.error("Game mode parameter is missing");
            }

            // Resolve any placeholders in the game mode
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMode = resolver.resolve(context, modeValue);
            
            // Parse game mode parameter
            String modeName = resolvedMode.asString();
            if (modeName == null || modeName.isEmpty()) {
                return ExecutionResult.error("Game mode name is empty or null");
            }

            // Set the game mode
            try {
                GameMode gameMode = GameMode.valueOf(modeName.toUpperCase());
                player.setGameMode(gameMode);
                return ExecutionResult.success("Game mode set to " + modeName);
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid game mode: " + modeName);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set game mode: " + e.getMessage());
        }
    }
}