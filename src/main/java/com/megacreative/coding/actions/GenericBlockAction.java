package com.megacreative.coding.actions;

import com.megacreative.coding.BlockConfig;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;

/**
 * A generic block action that can be dynamically created from configuration.
 * This allows for runtime-defined actions without requiring code changes.
 */
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
        context.getLogger().info("Executing generic action: " + config.getName());
        
        // Process parameters from the block
        // This is a simple example - you can extend this to handle
        // different types of actions based on the configuration
        
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
                return ExecutionResult.failure("Unknown action type: " + actionType);
        }
    }
    
    private ExecutionResult handleMessageAction(CodeBlock block, ExecutionContext context) {
        // Example: Send a message to the player
        String message = config.getParameters().getString("message", "No message set");
        if (context.getPlayer() != null) {
            context.getPlayer().sendMessage(message);
            return ExecutionResult.success("Message sent: " + message);
        }
        return ExecutionResult.failure("No player available to send message to");
    }
    
    private ExecutionResult handleCommandAction(CodeBlock block, ExecutionContext context) {
        // Example: Execute a command
        String command = config.getParameters().getString("command", "");
        if (!command.isEmpty()) {
            boolean success = context.getServer().dispatchCommand(
                context.getPlayer() != null ? context.getPlayer() : context.getServer().getConsoleSender(),
                command
            );
            return success ? 
                ExecutionResult.success("Command executed: " + command) :
                ExecutionResult.failure("Failed to execute command: " + command);
        }
        return ExecutionResult.failure("No command specified");
    }
    
    @Override
    public String toString() {
        return "GenericBlockAction{" +
                "name='" + config.getName() + '\'' +
                ", type='" + config.getActionName() + '\'' +
                '}';
    }
}
