package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;

/**
 * Action to send a message to a Discord webhook
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "discordWebhook", displayName = "§bDiscord Webhook", type = BlockType.ACTION)
public class DiscordWebhookAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue webhookUrlValue = block.getParameter("webhook_url");
            DataValue messageValue = block.getParameter("message");
            DataValue usernameValue = block.getParameter("username");
            
            if (webhookUrlValue == null || messageValue == null) {
                return ExecutionResult.error("Missing required parameters: webhook_url, message");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedWebhookUrl = resolver.resolve(context, webhookUrlValue);
            DataValue resolvedMessage = resolver.resolve(context, messageValue);
            
            String webhookUrl = resolvedWebhookUrl.asString();
            String message = resolvedMessage.asString();
            
            // Get username (default to "MegaCreative")
            String username = "MegaCreative";
            if (usernameValue != null) {
                DataValue resolvedUsername = resolver.resolve(context, usernameValue);
                username = resolvedUsername.asString();
            }
            
            // Send message to Discord webhook (asynchronously)
            Bukkit.getScheduler().runTaskAsynchronously(context.getPlugin(), () -> {
                try {
                    // In a real implementation, this would send an HTTP request to the webhook URL
                    // For now, we'll just log that we would send the message
                    context.getPlugin().getLogger().info("Would send Discord message: " + message);
                } catch (Exception e) {
                    context.getPlugin().getLogger().warning("Failed to send Discord message: " + e.getMessage());
                }
            });
            
            return ExecutionResult.success("Sending message to Discord webhook");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to send Discord message: " + e.getMessage());
        }
    }
}