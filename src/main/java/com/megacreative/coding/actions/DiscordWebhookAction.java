package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@BlockMeta(id = "discordWebhook", displayName = "§aDiscord Webhook", type = BlockType.ACTION)
public class DiscordWebhookAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the block
            DataValue webhookUrlValue = block.getParameter("webhook_url");
            DataValue messageValue = block.getParameter("message");
            DataValue usernameValue = block.getParameter("username", DataValue.of("MegaCreative"));
            
            if (webhookUrlValue == null || webhookUrlValue.isEmpty()) {
                return ExecutionResult.error("Webhook URL parameter is missing.");
            }
            
            if (messageValue == null || messageValue.isEmpty()) {
                return ExecutionResult.error("Message parameter is missing.");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedWebhookUrl = resolver.resolve(context, webhookUrlValue);
            DataValue resolvedMessage = resolver.resolve(context, messageValue);
            DataValue resolvedUsername = resolver.resolve(context, usernameValue);
            
            String webhookUrl = resolvedWebhookUrl.asString();
            String message = resolvedMessage.asString();
            String username = resolvedUsername.asString();
            
            // Send message to Discord webhook asynchronously
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                return sendDiscordMessage(webhookUrl, message, username);
            });
            
            // Create a CompletableFuture that will complete with an ExecutionResult
            CompletableFuture<ExecutionResult> resultFuture = future.thenApply(success -> {
                if (success) {
                    return ExecutionResult.success("Message sent to Discord.");
                } else {
                    return ExecutionResult.error("Failed to send message to Discord.");
                }
            }).exceptionally(throwable -> {
                return ExecutionResult.error("Error sending message to Discord: " + throwable.getMessage());
            });
            
            // Return an await result - the ScriptEngine will handle the future
            return ExecutionResult.await(resultFuture);

        } catch (Exception e) {
            return ExecutionResult.error("Error sending message to Discord: " + e.getMessage());
        }
    }
    
    private boolean sendDiscordMessage(String webhookUrl, String message, String username) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            String json = "{\"content\":\"" + message.replace("\"", "\\\"") + "\",\"username\":\"" + username.replace("\"", "\\\"") + "\"}";
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            return responseCode == 200 || responseCode == 204;
        } catch (Exception e) {
            return false;
        }
    }
}