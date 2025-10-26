package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action that triggers when a player sends a chat message
 */
@BlockMeta(id = "onChat", displayName = "§bPlayer Chat", type = BlockType.EVENT)
public class OnChatAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        // Get the message from the block parameters
        DataValue messageValue = block.getParameter("message");
        if (messageValue == null) {
            return ExecutionResult.error("No message parameter found");
        }
        
        String message = messageValue.asString();
        
        // This is an event action, so we just log that it was triggered
        context.getPlugin().getLogger().fine("Player chat event triggered for " + player.getName() + ": " + message);
        return ExecutionResult.success("Player chat event processed");
    }
}