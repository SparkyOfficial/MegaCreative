package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class SubVarAction implements BlockAction {
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
                double toSubtract = Double.parseDouble(valueStr);
                
                context.setVariable(varName, String.valueOf(current - toSubtract));
                
            } catch (NumberFormatException e) {
                Player player = context.getPlayer();
                if (player != null) {
                    player.sendMessage("§cОшибка: значение для вычитания должно быть числом.");
                }
            }
        }
    }
} 