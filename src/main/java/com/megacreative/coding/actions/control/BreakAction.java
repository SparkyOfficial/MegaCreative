package com.megacreative.coding.actions.control;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

/**
 * 🎆 Reference System-Style Break Action
 * 
 * Handles break statements in loops.
 * Sets a break flag in the execution context to signal loop termination.
 */
@BlockMeta(id = "break", displayName = "§aBreak", type = BlockType.ACTION)
public class BreakAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public BreakAction(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (player == null) {
            return ExecutionResult.error("Invalid execution context for break");
        }
        
        try {
            // Set break flag in context to break out of loops
            context.setBreakFlag(true);
            
            plugin.getLogger().info("🎆 Break statement executed in loop");
            
            if (player != null) {
                player.sendMessage("§aBreak statement executed");
            }
            
            return ExecutionResult.success("Break executed").withPause();
            
        } catch (Exception e) {
            plugin.getLogger().warning(".EVT Break action failed: " + e.getMessage());
            return ExecutionResult.error("Break failed: " + e.getMessage());
        }
    }
}