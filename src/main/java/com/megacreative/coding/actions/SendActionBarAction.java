package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class SendActionBarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in context");
        }

        try {
            // Get message parameter
            DataValue messageValue = block.getParameter("message");
            if (messageValue == null || messageValue.isEmpty()) {
                return ExecutionResult.error("Message parameter is missing");
            }

            String message = messageValue.asString();
            
            // Send action bar message
            player.sendActionBar(message);

            return ExecutionResult.success("Sent action bar message");
        } catch (Exception e) {
            return ExecutionResult.error("Error sending action bar: " + e.getMessage());
        }
    }
}