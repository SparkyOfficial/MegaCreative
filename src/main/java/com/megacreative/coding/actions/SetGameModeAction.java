package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SetGameModeAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);

        // Получаем и разрешаем параметры
        DataValue rawMode = block.getParameter("mode");
        if (rawMode == null) return;

        String modeStr = resolver.resolve(context, rawMode).asString();

        if (modeStr == null) return;

        try {
            GameMode gameMode = GameMode.valueOf(modeStr.toUpperCase());
            player.setGameMode(gameMode);
            
            player.sendMessage("§a🎮 Режим игры изменен на: " + gameMode.name());
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cНеизвестный режим игры: " + modeStr);
        }
    }
} 