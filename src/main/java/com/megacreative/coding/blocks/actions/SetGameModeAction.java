package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SetGameModeAction implements BlockAction {
    
    private final Argument<TextValue> gameModeArgument;
    
    public SetGameModeAction() {
        this.gameModeArgument = new ParameterArgument("gameMode");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        try {
            String gameModeStr = gameModeArgument.parse(context.getCurrentBlock()).get().get(context);
            GameMode gameMode = GameMode.valueOf(gameModeStr.toUpperCase());
            
            Player player = context.getPlayer();
            player.setGameMode(gameMode);
            
            context.getPlugin().getLogger().info("Set game mode to " + gameMode.name() + " for player " + player.getName());
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Failed to set game mode: " + e.getMessage());
        }
    }
} 