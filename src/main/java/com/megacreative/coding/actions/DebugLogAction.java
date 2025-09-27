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
import java.util.logging.Level;

@BlockMeta(id = "debugLog", displayName = "§aDebug Log", type = BlockType.ACTION)
public class DebugLogAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("");
        }

        try {
            // Get parameters from the block
            DataValue messageValue = block.getParameter("message");
            DataValue levelValue = block.getParameter("level", DataValue.of("INFO"));
            
            if (messageValue == null || messageValue.isEmpty()) {
                return ExecutionResult.error("");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMessage = resolver.resolve(context, messageValue);
            DataValue resolvedLevel = resolver.resolve(context, levelValue);
            
            String message = resolvedMessage.asString();
            String level = resolvedLevel.asString().toUpperCase();
            
            // Logging is disabled
            
            return ExecutionResult.success("");

        } catch (Exception e) {
            return ExecutionResult.error("");
        }
    }
}