package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class AddVarAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        CodeBlock block = context.getCurrentBlock();
        if (block == null) return;
        
        String varName = (String) block.getParameter("var");
        String valueStr = (String) block.getParameter("value");

        if (varName != null && valueStr != null) {
            try {
                // Получаем текущее значение или 0, если его нет
                Object currentValue = context.getVariable(varName);
                double current = currentValue != null ? Double.parseDouble(currentValue.toString()) : 0.0;
                double toAdd = Double.parseDouble(valueStr);
                
                context.setVariable(varName, String.valueOf(current + toAdd));
                
            } catch (NumberFormatException e) {
                Player player = context.getPlayer();
                if (player != null) {
                    player.sendMessage("§cОшибка: значение для сложения должно быть числом.");
                }
            }
        }
    }
} 