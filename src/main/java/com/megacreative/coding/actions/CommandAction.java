package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Action for executing a command.
 * This action retrieves a command from the block parameters and executes it.
 */
public class CommandAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the command parameter from the block
            DataValue commandValue = block.getParameter("command");
            if (commandValue == null) {
                return ExecutionResult.error("Command parameter is missing");
            }

            // Resolve any placeholders in the command
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedCommand = resolver.resolve(context, commandValue);
            
            // Execute the command
            String command = resolvedCommand.asString();
            if (command != null && !command.isEmpty()) {
                boolean success = Bukkit.dispatchCommand(player, command);
                if (success) {
                    return ExecutionResult.success("Command executed successfully");
                } else {
                    return ExecutionResult.error("Failed to execute command: " + command);
                }
            } else {
                return ExecutionResult.error("Command is empty or null");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to execute command: " + e.getMessage());
        }
    }
}