package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class HealPlayerAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return;
        
        Object rawAmount = block.getParameter("amount");
        String amountStr = ParameterResolver.resolve(context, rawAmount).toString();
        
        try {
            double amount = Double.parseDouble(amountStr);
            double currentHealth = player.getHealth();
            double maxHealth = player.getMaxHealth();
            double newHealth = Math.min(currentHealth + amount, maxHealth);
            
            player.setHealth(newHealth);
            player.sendMessage("§a✓ Вы восстановили " + (newHealth - currentHealth) + " здоровья!");
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: количество здоровья должно быть числом!");
        }
    }
} 