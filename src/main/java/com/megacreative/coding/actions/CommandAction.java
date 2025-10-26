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
 * Action to execute a command
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "command", displayName = "§bExecute Command", type = BlockType.ACTION)
public class CommandAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
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
            
            // Execute command
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandStr);
            
            if (success) {
                return ExecutionResult.success("Executed command: " + commandStr);
            } else {
                return ExecutionResult.error("Failed to execute command: " + commandStr);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to execute command: " + e.getMessage());
        }
    }
}