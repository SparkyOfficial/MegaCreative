package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

/**
 * Action that triggers when a player dies
 */
@BlockMeta(id = "onPlayerDeath", displayName = "§bPlayer Death", type = BlockType.EVENT)
public class OnPlayerDeathAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        // This is an event action, so we just log that it was triggered
        context.getPlugin().getLogger().fine("Player death event triggered for " + player.getName());
        return ExecutionResult.success("Player death event processed");
    }
}