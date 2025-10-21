package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;

/**
 * Action for executing a server command.
 * This action retrieves a command and executes it on the server.
 * 
 * Действие для выполнения серверной команды.
 * Это действие получает команду и выполняет ее на сервере.
 * 
 * @author Андрій Budильников
 */
@BlockMeta(id = "command", displayName = "§aExecute Command", type = BlockType.ACTION)
public class CommandAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue commandValue = block.getParameter("command");
            
            if (commandValue == null || commandValue.isEmpty()) {
                return ExecutionResult.error("No command provided");
            }
            
            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedCommand = resolver.resolve(context, commandValue);
            
            String command = resolvedCommand.asString();
            
            
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            if (success) {
                return ExecutionResult.success("Command executed successfully");
            } else {
                return ExecutionResult.error("Failed to execute command: " + command);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error executing command: " + e.getMessage());
        }
    }
}