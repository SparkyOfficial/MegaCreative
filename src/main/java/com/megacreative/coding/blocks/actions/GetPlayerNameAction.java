package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.entity.Player;

public class GetPlayerNameAction implements BlockAction {
    
    private final Argument<TextValue> varNameArgument;
    
    public GetPlayerNameAction() {
        this.varNameArgument = new ParameterArgument("var");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        try {
            TextValue varNameValue = varNameArgument.parse(context.getCurrentBlock()).orElse(null);
            if (varNameValue == null) {
                player.sendMessage("§cОшибка: не указано имя переменной!");
                return;
            }
            
            String varName = varNameValue.get(context);
            String playerName = player.getName();
            
            context.setVariable(varName, playerName);
            player.sendMessage("§a✓ Имя игрока '" + playerName + "' сохранено в переменную '" + varName + "'");
            context.getPlugin().getLogger().info("Saved player name '" + playerName + "' to variable '" + varName + "' for player " + player.getName());
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Получить имя игрока': " + e.getMessage());
        }
    }
} 