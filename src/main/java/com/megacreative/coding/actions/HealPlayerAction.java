package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action to heal a player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "healPlayer", displayName = "§bHeal Player", type = BlockType.ACTION)
public class HealPlayerAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameter
            DataValue amountValue = block.getParameter("amount");
            
            // Resolve parameter
            double amount = player.getMaxHealth(); // Default to full heal
            if (amountValue != null) {
                ParameterResolver resolver = new ParameterResolver(context);
                DataValue resolvedAmount = resolver.resolve(context, amountValue);
                amount = Math.max(0, resolvedAmount.asNumber().doubleValue());
            }
            
            // Heal player
            double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + amount);
            player.setHealth(newHealth);
            
            return ExecutionResult.success("Healed player by " + amount + " health points");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to heal player: " + e.getMessage());
        }
    }
}