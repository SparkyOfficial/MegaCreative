package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

/**
 * Action for healing a player.
 * This action retrieves heal amount and applies it to the player.
 */
@BlockMeta(id = "healPlayer", displayName = "§aHeal Player", type = BlockType.ACTION)
public class HealPlayerAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            double healAmount = getHealAmount(block);
            double currentHealth = player.getHealth();
            double maxHealth = player.getMaxHealth();
            double newHealth = Math.min(maxHealth, currentHealth + healAmount);
            
            player.setHealth(newHealth);
            
            return ExecutionResult.success("Player healed successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to heal player: " + e.getMessage());
        }
    }
    
    private double getHealAmount(CodeBlock block) {
        com.megacreative.coding.values.DataValue value = block.getParameter("amount");
        if (value != null && !value.isEmpty()) {
            try {
                return Double.parseDouble(value.asString());
            } catch (NumberFormatException e) {
                // Log exception and continue processing
                // This is expected behavior when parsing user input
                // Return default value when parsing fails
            }
        }
        return 20.0; 
    }
}