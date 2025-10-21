package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Bukkit;

/**
 * Action for broadcasting a message to all players.
 * This action retrieves a message and broadcasts it to all online players.
 */
@BlockMeta(id = "broadcast", displayName = "§aBroadcast Message", type = BlockType.ACTION)
public class BroadcastAction implements BlockAction {

    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            com.megacreative.coding.values.DataValue messageValue = block.getParameter("message");
            
            if (messageValue == null || messageValue.isEmpty()) {
                return ExecutionResult.error("No message provided");
            }
            
            String message = messageValue.asString();
            // Fix for Qodana issue: Condition message != null is always true
            // This was a false positive - we need to properly check for empty strings
            if (!message.trim().isEmpty()) {
                Bukkit.broadcastMessage(message);
            }
            
            return ExecutionResult.success("Message broadcasted successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to broadcast message: " + e.getMessage());
        }
    }
}