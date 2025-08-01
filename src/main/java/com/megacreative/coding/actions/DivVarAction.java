package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class DivVarAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        CodeBlock block = context.getCurrentBlock();
        if (block == null) return;
        
        String varName = (String) block.getParameter("var");
        String valueStr = (String) block.getParameter("value");

        if (varName != null && valueStr != null) {
            try {
                Object currentValue = context.getVariable(varName);
                double current = currentValue != null ? Double.parseDouble(currentValue.toString()) : 0.0;
                double divisor = Double.parseDouble(valueStr);
                
                if (divisor == 0) {
                    Player player = context.getPlayer();
                    if (player != null) {
                        player.sendMessage("§cОшибка: деление на ноль!");
                    }
                    return;
                }
                
                context.setVariable(varName, String.valueOf(current / divisor));
                
            } catch (NumberFormatException e) {
                Player player = context.getPlayer();
                if (player != null) {
                    player.sendMessage("§cОшибка: значение для деления должно быть числом.");
                }
            }
        }
    }
} 