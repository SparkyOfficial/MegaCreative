package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.TargetSelector;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.MessagingService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

/**
 * Action that sends a message to a target player or players
 * Demonstrates the use of target selectors
 */
@BlockMeta(id = "sendMessageToTarget", displayName = "§aSend Message to Target", type = BlockType.ACTION)
public class SendMessageToTargetAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue messageValue = block.getParameter("message");
            DataValue targetValue = block.getParameter("target");
            
            if (messageValue == null || messageValue.asString() == null || messageValue.asString().trim().isEmpty()) {
                return ExecutionResult.error("No message provided");
            }
            
            String message = messageValue.asString().trim();
            
            // Parse target selector
            String targetSelectorStr = targetValue != null ? targetValue.asString() : "@s"; // Default to self
            TargetSelector targetSelector = TargetSelector.parse(targetSelectorStr);
            
            // Resolve targets
            java.util.List<Player> targets = targetSelector.resolveTargets(context);
            
            if (targets.isEmpty()) {
                return ExecutionResult.error("No targets found for selector: " + targetSelectorStr);
            }
            
            // Get messaging service
            MessagingService messagingService = context.getPlugin().getServiceRegistry().getMessagingService();
            
            for (Player target : targets) {
                // Replace placeholders
                String finalMessage = message
                    .replace("%player%", target.getName())
                    .replace("%sender%", context.getPlayer() != null ? context.getPlayer().getName() : "System");
                
                // Send message using Adventure API if available, otherwise fallback to regular sendMessage
                if (messagingService != null) {
                    messagingService.sendMessage(target, finalMessage);
                } else {
                    target.sendMessage(finalMessage);
                }
            }
            
            return ExecutionResult.success("Message sent to " + targets.size() + " player(s)");
            
        } catch (Exception e) {
            return ExecutionResult.error("Failed to send message: " + e.getMessage());
        }
    }
}