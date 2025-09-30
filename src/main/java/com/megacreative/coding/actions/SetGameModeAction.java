package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Action for setting a player's game mode.
 * This action changes the player's game mode based on parameters.
 */
@BlockMeta(id = "setGameMode", displayName = "§aSet Game Mode", type = BlockType.ACTION)
public class SetGameModeAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the game mode from the new parameter system
            DataValue modeValue = block.getParameter("mode");
            
            if (modeValue == null || modeValue.isEmpty()) {
                return ExecutionResult.error("Game mode is not configured");
            }

            // Parse game mode
            GameMode gameMode = GameMode.valueOf(modeValue.asString().toUpperCase());

            // Set the game mode
            player.setGameMode(gameMode);
            return ExecutionResult.success("Game mode set to " + gameMode.name());
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set game mode: " + e.getMessage());
        }
    }
}