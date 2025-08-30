package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class HealPlayerAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawAmount = block.getParameter("amount");
        
        if (rawAmount == null) return;
        
        String amountStr = resolver.resolve(context, rawAmount).asString();

        try {
            double amount = amountStr != null ? Double.parseDouble(amountStr) : player.getMaxHealth();
            
            double newHealth = Math.min(player.getHealth() + amount, player.getMaxHealth());
            player.setHealth(newHealth);
            
            player.sendMessage("§a❤ Исцеление на " + amount + " единиц здоровья!");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: количество здоровья должно быть числом");
        }
    }
} 