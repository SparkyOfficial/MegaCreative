package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import com.megacreative.services.MessagingService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

import java.util.function.Function;

/**
 * Unified action for sending messages to a player.
 * Supports chat messages, action bar messages, and titles based on the "type" parameter.
 * 
 * Parameters:
 * - "message": The message content (required)
 * - "type": The message type - "chat", "actionbar", "title", or "subtitle" (default: "chat")
 * - "subtitle": The subtitle content (required when type is "title")
 * - "fadeIn": Fade in time in ticks (used for titles, default: 10)
 * - "stay": Stay time in ticks (used for titles, default: 70)
 * - "fadeOut": Fade out time in ticks (used for titles, default: 20)
 */
@BlockMeta(id = "sendMessage", displayName = "§aSend Message", type = BlockType.ACTION)
public class SendMessageAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue messageValue = block.getParameter("message");
            if (messageValue == null || messageValue.isEmpty()) {
                return ExecutionResult.error("Message content is required");
            }
            String message = messageValue.asString();

            
            DataValue typeValue = block.getParameter("type", DataValue.of("chat"));
            String type = typeValue.asString().toLowerCase();

            
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedMessage = resolver.resolveString(context, message);

            
            switch (type) {
                case "actionbar":
                    player.sendActionBar(resolvedMessage);
                    context.getPlugin().getLogger().fine("Executing SendMessageAction - Sent action bar message to player " + player.getName() + ": " + resolvedMessage);
                    return ExecutionResult.success("Action bar message sent successfully");
                    
                case "title":
                    
                    DataValue subtitleValue = block.getParameter("subtitle");
                    String subtitle = "";
                    if (subtitleValue != null && !subtitleValue.isEmpty()) {
                        subtitle = resolver.resolveString(context, subtitleValue.asString());
                    }
                    
                    
                    int fadeIn = getTimingParameter(block, "fadeIn", 10);
                    int stay = getTimingParameter(block, "stay", 70);
                    int fadeOut = getTimingParameter(block, "fadeOut", 20);
                    
                    player.sendTitle(resolvedMessage, subtitle, fadeIn, stay, fadeOut);
                    context.getPlugin().getLogger().fine("Executing SendMessageAction - Sent title to player " + player.getName() + ": " + resolvedMessage + " | " + subtitle);
                    return ExecutionResult.success("Title sent successfully");
                    
                case "chat":
                default:
                    
                    MessagingService messagingService = context.getPlugin().getServiceRegistry().getMessagingService();
                    if (messagingService != null) {
                        messagingService.sendMessage(player, resolvedMessage);
                    } else {
                        
                        player.sendMessage(resolvedMessage);
                    }
                    context.getPlugin().getLogger().fine("Executing SendMessageAction - Sent chat message to player " + player.getName() + ": " + resolvedMessage);
                    return ExecutionResult.success("Chat message sent successfully");
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Executing SendMessageAction - Failed to send message: " + e.getMessage());
            return ExecutionResult.error("Failed to send message: " + e.getMessage());
        }
    }
    
    /**
     * Gets timing parameter with validation
     */
    private int getTimingParameter(CodeBlock block, String paramName, int defaultValue) {
        DataValue value = block.getParameter(paramName);
        if (value != null && !value.isEmpty()) {
            try {
                return Math.max(0, value.asNumber().intValue());
            } catch (NumberFormatException e) {
                // Log exception and continue processing
                // This is expected behavior when parsing user input
                // Use default value when parsing fails
            }
        }
        return defaultValue;
    }
}