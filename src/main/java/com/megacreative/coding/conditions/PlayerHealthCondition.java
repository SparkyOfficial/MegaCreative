package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

/**
 * Condition for checking player health.
 * This condition returns true if the player's health meets the specified criteria, false otherwise.
 */
@BlockMeta(id = "playerHealth", displayName = "§aPlayer Health", type = BlockType.CONDITION)
public class PlayerHealthCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            
            com.megacreative.coding.values.DataValue healthValue = block.getParameter("health");
            if (healthValue == null || healthValue.isEmpty()) {
                return false;
            }
            
            double requiredHealth = Double.parseDouble(healthValue.asString());
            double playerHealth = player.getHealth();
            
            
            return playerHealth >= requiredHealth;
        } catch (Exception e) {
            
            return false;
        }
    }
}