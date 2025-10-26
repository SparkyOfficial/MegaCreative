package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Action to set a player's game mode
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "setGameMode", displayName = "§bSet Game Mode", type = BlockType.ACTION)
public class SetGameModeAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameter
            DataValue modeValue = block.getParameter("mode");
            
            if (modeValue == null) {
                return ExecutionResult.error("Missing required parameter: mode");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMode = resolver.resolve(context, modeValue);
            
            String modeStr = resolvedMode.asString();
            GameMode gameMode;
            
            try {
                gameMode = GameMode.valueOf(modeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid game mode: " + modeStr);
            }
            
            // Set game mode
            player.setGameMode(gameMode);
            
            return ExecutionResult.success("Set game mode to " + gameMode.name());
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set game mode: " + e.getMessage());
        }
    }
}