package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class MulVarAction implements BlockAction {
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
                double multiplier = Double.parseDouble(valueStr);
                
                context.setVariable(varName, String.valueOf(current * multiplier));
                
            } catch (NumberFormatException e) {
                Player player = context.getPlayer();
                if (player != null) {
                    player.sendMessage("§cОшибка: значение для умножения должно быть числом.");
                }
            }
        }
    }
} 