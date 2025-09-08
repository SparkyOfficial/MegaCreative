package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.TargetSelector;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action that sends a message to a target player or players
 * Demonstrates the use of target selectors
 */
public class SendMessageToTargetAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue messageValue = block.getParameter("message");
            DataValue targetValue = block.getParameter("target");
            
            if (messageValue == null) {
                return ExecutionResult.error("No message specified");
            }
            
            String message = messageValue.asString();
            if (message == null || message.trim().isEmpty()) {
                return ExecutionResult.error("Message cannot be empty");
            }
            
            // Parse target selector
            String targetSelectorStr = targetValue != null ? targetValue.asString() : "@s"; // Default to self
            TargetSelector targetSelector = TargetSelector.parse(targetSelectorStr);
            
            // Resolve targets
            java.util.List<Player> targets = targetSelector.resolveTargets(context);
            
            if (targets.isEmpty()) {
                return ExecutionResult.error("No targets found for selector: " + targetSelectorStr);
            }
            
            // Send message to all targets
            for (Player target : targets) {
                // Replace placeholders
                String finalMessage = message
                    .replace("%player%", target.getName())
                    .replace("%sender%", context.getPlayer() != null ? context.getPlayer().getName() : "System");
                
                target.sendMessage(finalMessage);
            }
            
            return ExecutionResult.success("Message sent to " + targets.size() + " player(s)");
            
        } catch (Exception e) {
            return ExecutionResult.error("Failed to send message to target: " + e.getMessage());
        }
    }
}