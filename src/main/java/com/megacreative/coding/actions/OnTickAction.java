package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

/**
 * Action that triggers on server tick
 */
@BlockMeta(id = "onTick", displayName = "§bServer Tick", type = BlockType.EVENT)
public class OnTickAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        // This is a server tick event, so there might not be a player
        Player player = context.getPlayer();
        String playerName = player != null ? player.getName() : "Unknown";
        
        // This is an event action, so we just log that it was triggered
        context.getPlugin().getLogger().fine("Server tick event triggered for player " + playerName);
        return ExecutionResult.success("Server tick event processed");
    }
}