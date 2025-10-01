package com.megacreative.coding.actions.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
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

            // Get the menu variable parameter
            DataValue menuValue = block.getParameter("menu");
            if (menuValue == null) {
                // Close the player's inventory directly
                targetPlayer.closeInventory();
                return ExecutionResult.success("Closed menu for player " + targetPlayer.getName());
            }
            
            String menuVariableName = menuValue.asString();
            if (menuVariableName == null || menuVariableName.isEmpty()) {
                // Close the player's inventory directly
                targetPlayer.closeInventory();
                return ExecutionResult.success("Closed menu for player " + targetPlayer.getName());
            }

            // Get the GUI inventory from the global variable
            VariableManager variableManager = plugin.getServiceRegistry().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager not available");
            }
            
            DataValue menuDataValue = variableManager.getGlobalVariable(menuVariableName);
            if (menuDataValue == null || menuDataValue.getValue() == null) {
                return ExecutionResult.error("Menu variable '" + menuVariableName + "' not found or is null");
            }
            
            if (!(menuDataValue.getValue() instanceof CreateMenuAction.GUIInventory)) {
                return ExecutionResult.error("Variable '" + menuVariableName + "' does not contain a valid menu");
            }
            
            // Close the player's inventory
            targetPlayer.closeInventory();
            
            return ExecutionResult.success("Closed menu for player " + targetPlayer.getName());
            
        } catch (Exception e) {
            return ExecutionResult.error("Error closing menu: " + e.getMessage());
        }
    }
}