package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * 🎆 FrameLand-Style Return Action
 * 
 * Handles function return statements with optional return values.
 * Terminates function execution and returns control to the caller.
 */
public class ReturnAction extends CodeAction {
    
    public ReturnAction(MegaCreative plugin) {
        super(plugin);
    }

    @Override
    public CompletableFuture<ExecutionResult> execute(ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (player == null) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Invalid execution context for return"));
        }
        
        try {
            // Get return value from block parameters
            Object returnValue = null;
            if (context.getCurrentBlock() != null) {
                returnValue = context.getCurrentBlock().getParameterValue("return_value");
            }
            
            // Set return value in context
            if (returnValue != null) {
                context.setVariable("return", returnValue);
                plugin.getLogger().info("🎆 Function returning with value: " + returnValue);
            } else {
                plugin.getLogger().info("🎆 Function returning without value");
            }
            
            // Create return result that signals function termination
            ExecutionResult result = ExecutionResult.success("Function returned");
            result.setTerminated(true); // Signal that execution should stop
            if (returnValue != null) {
                result.setReturnValue(returnValue);
            }
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            plugin.getLogger().warning("🎆 Return action failed: " + e.getMessage());
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Return failed: " + e.getMessage()));
        }
    }

    @Override
    public boolean canExecute(ExecutionContext context) {
        return context.getPlayer() != null;
    }

    @Override
    public String getActionName() {
        return "return";
    }

    @Override
    public String getDescription() {
        return "🎆 Returns from a function with an optional value";
    }
}