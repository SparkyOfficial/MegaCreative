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
 * Action to broadcast a message to all players
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "broadcast", displayName = "§bBroadcast Message", type = BlockType.ACTION)
public class BroadcastAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameter
            DataValue messageValue = block.getParameter("message");
            
            if (messageValue == null) {
                return ExecutionResult.error("Missing required parameter: message");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMessage = resolver.resolve(context, messageValue);
            
            String message = resolvedMessage.asString();
            
            // Broadcast message
            Bukkit.broadcastMessage(message);
            
            return ExecutionResult.success("Broadcasted message: " + message);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to broadcast message: " + e.getMessage());
        }
    }
}