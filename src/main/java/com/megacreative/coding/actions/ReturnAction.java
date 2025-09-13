package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * 🎆 Reference System-Style Return Action
 * 
 * Handles function return statements with optional return values.
 * Terminates function execution and returns control to the caller.
 */
public class ReturnAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public ReturnAction(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (player == null) {
            return ExecutionResult.error("Invalid execution context for return");
        }
        
        try {
            // Get return value from block parameters
            Object returnValue = null;
            if (block != null) {
                returnValue = block.getParameterValue("return_value");
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
            
            return result;
            
        } catch (Exception e) {
            plugin.getLogger().warning("🎆 Return action failed: " + e.getMessage());
            return ExecutionResult.error("Return failed: " + e.getMessage());
        }
    }
}