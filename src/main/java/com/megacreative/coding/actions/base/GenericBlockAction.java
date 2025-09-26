package com.megacreative.coding.actions.base;

import com.megacreative.coding.config.BlockConfig;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

/**
 * A generic block action that can be dynamically created from configuration.
 * This allows for runtime-defined actions without requiring code changes.
 */
@BlockMeta(id = "genericBlock", displayName = "§aGeneric Block", type = BlockType.ACTION)
public class GenericBlockAction implements BlockAction {
    private final BlockConfig config;
    
    public GenericBlockAction(BlockConfig config) {
        this.config = config;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        // Default implementation - can be overridden by subclasses
        // or configured via the BlockConfig
        
        // Log the execution for debugging
        context.getPlugin().getLogger().info("Executing generic action: " + config.getName());
        
        // Get the action type from the configuration
        String actionType = config.getActionName();
        
        // Process based on action type
        switch (actionType.toUpperCase()) {
            case "MESSAGE":
                return handleMessageAction(block, context);
            case "COMMAND":
                return handleCommandAction(block, context);
            // Add more action types as needed
            default:
                return ExecutionResult.error("Unknown action type: " + actionType);
        }
    }
    
    private ExecutionResult handleMessageAction(CodeBlock block, ExecutionContext context) {
        // Example: Send a message to the player
        Object messageObj = block.getParameter("message");
        String message = messageObj != null ? messageObj.toString() : "No message set";
        
        if (context.getPlayer() != null) {
            context.getPlayer().sendMessage(message);
            return ExecutionResult.success("Message sent: " + message);
        }
        return ExecutionResult.error("No player available to send message to");
    }
    
    private ExecutionResult handleCommandAction(CodeBlock block, ExecutionContext context) {
        // Example: Execute a command
        Object commandObj = block.getParameter("command");
        String command = commandObj != null ? commandObj.toString() : null;
        
        if (command != null && !command.isEmpty()) {
            boolean success = context.getPlugin().getServer().dispatchCommand(
                context.getPlayer() != null ? context.getPlayer() : context.getPlugin().getServer().getConsoleSender(),
                command
            );
            return success ? 
                ExecutionResult.success("Command executed: " + command) :
                ExecutionResult.error("Failed to execute command: " + command);
        }
        return ExecutionResult.error("No command specified");
    }
    
    @Override
    public String toString() {
        return "GenericBlockAction{" +
                "name='" + config.getName() + '\'' +
                ", type='" + config.getActionName() + '\'' +
                '}';
    }
}