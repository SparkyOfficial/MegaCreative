package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.entity.Player;

public class GetVarAction implements BlockAction {
    
    private final Argument<TextValue> varNameArgument;
    
    public GetVarAction() {
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
            Object varValue = context.getVariable(varName);
            
            if (varValue != null) {
                String varValueStr = varValue.toString();
                player.sendMessage("§aЗначение переменной '" + varName + "': " + varValueStr);
                context.getPlugin().getLogger().info("Retrieved variable '" + varName + "' = '" + varValueStr + "' for player " + player.getName());
            } else {
                player.sendMessage("§cПеременная '" + varName + "' не найдена");
                context.getPlugin().getLogger().warning("Variable '" + varName + "' not found for player " + player.getName());
            }
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Получить переменную': " + e.getMessage());
        }
    }
} 