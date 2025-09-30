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
import org.bukkit.entity.Player;

/**
 * Action for executing a command asynchronously.
 * This action retrieves a command from the new parameter system and executes it asynchronously.
 */
@BlockMeta(id = "executeAsyncCommand", displayName = "§aExecute Async Command", type = BlockType.ACTION)
public class ExecuteAsyncCommandAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player available to execute command");
        }

        try {
            // Get command parameter from the new parameter system
            DataValue commandValue = block.getParameter("command");
            if (commandValue == null || commandValue.isEmpty()) {
                return ExecutionResult.error("No command provided");
            }

            // Resolve any placeholders in the command
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedCommand = resolver.resolve(context, commandValue);

            // Execute the command with proper thread safety
            Bukkit.getScheduler().runTask(context.getPlugin(), () -> {
                try {
                    Bukkit.dispatchCommand(player, resolvedCommand.asString());
                } catch (Exception e) {
                    context.getPlugin().getLogger().warning("Error executing async command: " + e.getMessage());
                }
            });
            
            return ExecutionResult.success("Async command scheduled for execution");
        } catch (Exception e) {
            return ExecutionResult.error("Error scheduling async command: " + e.getMessage());
        }
    }
}