package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExecuteAsyncCommandAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the block
            DataValue commandValue = block.getParameter("command");
            DataValue delayValue = block.getParameter("delay", DataValue.of(0));
            
            if (commandValue == null || commandValue.isEmpty()) {
                return ExecutionResult.error("Command parameter is missing.");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedCommand = resolver.resolve(context, commandValue);
            DataValue resolvedDelay = resolver.resolve(context, delayValue);
            
            String command = resolvedCommand.asString();
            int delay = resolvedDelay.asNumber().intValue();
            
            // Execute command asynchronously with optional delay
            Bukkit.getScheduler().runTaskLaterAsynchronously(context.getPlugin(), () -> {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                } catch (Exception e) {
                    context.getPlugin().getLogger().severe("Error executing async command: " + e.getMessage());
                }
            }, delay);
            
            return ExecutionResult.success("Command will be executed asynchronously.");

        } catch (Exception e) {
            return ExecutionResult.error("Error executing async command: " + e.getMessage());
        }
    }
}