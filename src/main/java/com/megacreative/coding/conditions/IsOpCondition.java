package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta; // Added import
import com.megacreative.coding.BlockType; // Added import
import org.bukkit.entity.Player;

/**
 * Condition for checking if a player is an operator.
 * This condition returns true if the player is an operator, false otherwise.
 */
@BlockMeta(id = "isOp", displayName = "§aIs OP", type = BlockType.CONDITION) // Added annotation
public class IsOpCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Check if the player is an operator
            return player.isOp();
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}