package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action for sending a message to a player.
 * This action retrieves a message from the block parameters and sends it to the player.
 */
public class SendMessageAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the message parameter from the block
            DataValue messageValue = block.getParameter("message");
            if (messageValue == null) {
                return ExecutionResult.error("Message parameter is missing");
            }

            // Resolve any placeholders in the message
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMessage = resolver.resolve(context, messageValue);
            
            // Send the message to the player
            String message = resolvedMessage.asString();
            if (message != null && !message.isEmpty()) {
                player.sendMessage(message);
                return ExecutionResult.success("Message sent successfully");
            } else {
                return ExecutionResult.error("Message is empty or null");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to send message: " + e.getMessage());
        }
    }
}
