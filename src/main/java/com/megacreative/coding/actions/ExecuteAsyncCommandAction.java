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
 * Action to execute a command asynchronously
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "executeAsyncCommand", displayName = "§bExecute Async Command", type = BlockType.ACTION)
public class ExecuteAsyncCommandAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameter
            DataValue commandValue = block.getParameter("command");
            
            if (commandValue == null) {
                return ExecutionResult.error("Missing required parameter: command");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedCommand = resolver.resolve(context, commandValue);
            
            String commandStr = resolvedCommand.asString();
            
            // Execute command asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(context.getPlugin(), () -> {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandStr);
                } catch (Exception e) {
                    context.getPlugin().getLogger().warning("Failed to execute async command: " + e.getMessage());
                }
            });
            
            return ExecutionResult.success("Executing command asynchronously: " + commandStr);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to execute async command: " + e.getMessage());
        }
    }
}