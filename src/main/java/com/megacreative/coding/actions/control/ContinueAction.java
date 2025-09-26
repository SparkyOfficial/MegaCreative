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
 * 🎆 Reference System-Style Continue Action
 * 
 * Handles continue statements in loops.
 * Sets a continue flag in the execution context to signal loop continuation.
 */
@BlockMeta(id = "continue", displayName = "§aContinue", type = BlockType.ACTION)
public class ContinueAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public ContinueAction(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (player == null) {
            return ExecutionResult.error("Invalid execution context for continue");
        }
        
        try {
            // Set continue flag in context to continue loops
            context.setContinueFlag(true);
            
            plugin.getLogger().info("🎆 Continue statement executed in loop");
            
            if (player != null) {
                player.sendMessage("§aContinue statement executed");
            }
            
            return ExecutionResult.success("Continue executed").withPause();
            
        } catch (Exception e) {
            plugin.getLogger().warning("🎆 Continue action failed: " + e.getMessage());
            return ExecutionResult.error("Continue failed: " + e.getMessage());
        }
    }
}