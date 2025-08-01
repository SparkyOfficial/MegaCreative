package com.megacreative.coding.actions;

import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class IsOpCondition implements BlockCondition {
    @Override
    public boolean check(ExecutionContext context) {
        Player player = context.getPlayer();
        return player != null && player.isOp();
    }
} 