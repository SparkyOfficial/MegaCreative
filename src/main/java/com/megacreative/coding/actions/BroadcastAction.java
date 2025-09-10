package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;

/**
 * Action for broadcasting a message to all players.
 * This action retrieves a message from the block parameters and broadcasts it.
 */
public class BroadcastAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get the message parameter from the block
            DataValue messageValue = block.getParameter("message");
            if (messageValue == null) {
                return ExecutionResult.error("Message parameter is missing");
            }

            // Resolve any placeholders in the message
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMessage = resolver.resolve(context, messageValue);
            
            // Broadcast the message
            String message = resolvedMessage.asString();
            if (message != null && !message.isEmpty()) {
                Bukkit.broadcastMessage(message);
                return ExecutionResult.success("Message broadcasted successfully");
            } else {
                return ExecutionResult.error("Message is empty or null");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to broadcast message: " + e.getMessage());
        }
    }
}