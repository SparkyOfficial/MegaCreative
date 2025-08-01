package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SetGameModeAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return;
        
        Object rawMode = block.getParameter("mode");
        String modeStr = ParameterResolver.resolve(context, rawMode).toString();
        
        try {
            GameMode gameMode = GameMode.valueOf(modeStr.toUpperCase());
            player.setGameMode(gameMode);
            player.sendMessage("§a✓ Режим игры изменен на: " + gameMode.name());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cОшибка: неизвестный режим игры: " + modeStr);
            player.sendMessage("§7Доступные режимы: SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR");
        }
    }
} 