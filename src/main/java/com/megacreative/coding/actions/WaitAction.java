package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.ScriptEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import com.megacreative.MegaCreative;

/**
 * Action for waiting for a specified amount of time.
 * This action pauses execution for the specified duration.
 */
@BlockMeta(id = "wait", displayName = "§aWait", type = BlockType.ACTION)
public class WaitAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get duration parameter
            com.megacreative.coding.values.DataValue durationValue = block.getParameter("duration");
            
            if (durationValue == null || durationValue.isEmpty()) {
                return ExecutionResult.error("Duration is not configured");
            }
            
            long duration = Long.parseLong(durationValue.asString());
            
            // Convert milliseconds to ticks (1 tick = 50 milliseconds)
            long ticks = Math.max(1, duration / 50);
            
            // Get the next block to execute after waiting
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock == null) {
                return ExecutionResult.success("Wait completed with no next block to execute");
            }
            
            // Get the script engine
            ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
            if (scriptEngine == null) {
                return ExecutionResult.error("Script engine not available for delayed execution");
            }
            
            // Schedule execution of the next block after the delay
            Bukkit.getScheduler().runTaskLater(context.getPlugin(), () -> {
                scriptEngine.executeBlockChain(nextBlock, context.getPlayer(), "wait_action");
            }, ticks);
            
            // Return success immediately - the rest of the chain will execute later
            return ExecutionResult.success("Wait scheduled for " + duration + " milliseconds (" + ticks + " ticks)");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to wait: " + e.getMessage());
        }
    }
}