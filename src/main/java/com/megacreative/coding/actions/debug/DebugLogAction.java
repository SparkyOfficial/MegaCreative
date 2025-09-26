package com.megacreative.coding.actions.debug;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import java.util.logging.Level;

@BlockMeta(id = "debugLog", displayName = "§aDebug Log", type = BlockType.ACTION)
public class DebugLogAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the block
            DataValue messageValue = block.getParameter("message");
            DataValue levelValue = block.getParameter("level", DataValue.of("INFO"));
            
            if (messageValue == null || messageValue.isEmpty()) {
                return ExecutionResult.error("Message parameter is missing.");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMessage = resolver.resolve(context, messageValue);
            DataValue resolvedLevel = resolver.resolve(context, levelValue);
            
            String message = resolvedMessage.asString();
            String level = resolvedLevel.asString().toUpperCase();
            
            // Log the message with the specified level
            switch (level) {
                case "SEVERE":
                case "ERROR":
                    context.getPlugin().getLogger().severe("[DEBUG] " + message);
                    break;
                case "WARNING":
                case "WARN":
                    context.getPlugin().getLogger().warning("[DEBUG] " + message);
                    break;
                case "INFO":
                default:
                    context.getPlugin().getLogger().info("[DEBUG] " + message);
                    break;
            }
            
            return ExecutionResult.success("Debug information logged.");

        } catch (Exception e) {
            return ExecutionResult.error("Error logging debug information: " + e.getMessage());
        }
    }
}