package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action for healing a player.
 * This action heals the player by a specified amount or to full health.
 */
public class HealPlayerAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the heal amount parameter from the block (optional)
            DataValue amountValue = block.getParameter("amount");
            
            if (amountValue != null) {
                // Heal by specific amount
                ParameterResolver resolver = new ParameterResolver(context);
                DataValue resolvedAmount = resolver.resolve(context, amountValue);
                
                try {
                    double amount = resolvedAmount.asNumber().doubleValue();
                    double newHealth = Math.min(player.getHealth() + amount, player.getMaxHealth());
                    player.setHealth(newHealth);
                    return ExecutionResult.success("Player healed by " + amount + " points");
                } catch (NumberFormatException e) {
                    return ExecutionResult.error("Invalid heal amount: " + resolvedAmount.asString());
                }
            } else {
                // Heal to full health
                player.setHealth(player.getMaxHealth());
                return ExecutionResult.success("Player healed to full health");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to heal player: " + e.getMessage());
        }
    }
}