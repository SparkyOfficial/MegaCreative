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

/**
 * Action to send an action bar message to a player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "sendActionBar", displayName = "§bSend Action Bar", type = BlockType.ACTION)
public class SendActionBarAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
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
            
            // Send action bar message
            player.sendActionBar(message);
            
            return ExecutionResult.success("Sent action bar message to player");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to send action bar message: " + e.getMessage());
        }
    }
}