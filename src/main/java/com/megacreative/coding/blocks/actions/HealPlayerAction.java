package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.entity.Player;

public class HealPlayerAction implements BlockAction {
    
    private final Argument<TextValue> healAmountArgument;
    
    public HealPlayerAction() {
        this.healAmountArgument = new ParameterArgument("amount");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        try {
            String amountStr = healAmountArgument.parse(context.getCurrentBlock()).get().get(context);
            double healAmount = Double.parseDouble(amountStr);
            
            Player player = context.getPlayer();
            double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + healAmount);
            player.setHealth(newHealth);
            
            context.getPlugin().getLogger().info("Healed player " + player.getName() + " by " + healAmount + " health points");
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Failed to heal player: " + e.getMessage());
        }
    }
} 