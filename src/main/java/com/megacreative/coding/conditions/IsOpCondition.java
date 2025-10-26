package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a player is an operator.
 * This condition returns true if the player is an operator, false otherwise.
 */
@BlockMeta(id = "isOp", displayName = "§aIs Operator", type = BlockType.CONDITION)
public class IsOpCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            return player.isOp();
        } catch (Exception e) {
            return false;
        }
    }
}