package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Bukkit;

/**
 * Action for executing a server command.
 * This action retrieves a command and executes it on the server.
 */
@BlockMeta(id = "command", displayName = "§aExecute Command", type = BlockType.ACTION)
public class CommandAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get command parameter
            com.megacreative.coding.values.DataValue commandValue = block.getParameter("command");
            
            if (commandValue == null || commandValue.isEmpty()) {
                return ExecutionResult.error("");
            }
            
            String command = commandValue.asString();
            
            // Execute the command
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            if (success) {
                return ExecutionResult.success("");
            } else {
                return ExecutionResult.error("");
            }
        } catch (Exception e) {
            return ExecutionResult.error("");
        }
    }
}