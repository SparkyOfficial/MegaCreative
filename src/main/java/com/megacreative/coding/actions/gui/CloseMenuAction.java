package com.megacreative.coding.actions.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action to close a player's open menu
 */
@BlockMeta(
    id = "close_menu",
    displayName = "Close Menu",
    type = com.megacreative.coding.BlockType.ACTION
)
public class CloseMenuAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public CloseMenuAction(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get the player parameter, or use the context player if not specified
            DataValue playerValue = block.getParameter("player");
            Player targetPlayer = context.getPlayer();
            
            if (playerValue != null && playerValue.getValue() instanceof Player) {
                targetPlayer = (Player) playerValue.getValue();
            }
            
            if (targetPlayer == null) {
                return ExecutionResult.error("No player available to close menu for");
            }
            
            // Close the player's inventory
            targetPlayer.closeInventory();
            
            return ExecutionResult.success("Closed menu for player " + targetPlayer.getName());
            
        } catch (Exception e) {
            return ExecutionResult.error("Error closing menu: " + e.getMessage());
        }
    }
}