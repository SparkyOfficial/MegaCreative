package com.megacreative.coding.actions.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;

/**
 * Action to show a previously created menu to a player
 */
@BlockMeta(
    id = "show_menu",
    displayName = "Show Menu to Player",
    type = com.megacreative.coding.BlockType.ACTION
)
public class ShowMenuAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public ShowMenuAction(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue menuVariableValue = block.getParameter("menuVariable");
            DataValue playerValue = block.getParameter("player");
            
            if (menuVariableValue == null) {
                return ExecutionResult.error("Missing required parameter: menuVariable");
            }
            
            String menuVariableName = menuVariableValue.asString();
            if (menuVariableName == null || menuVariableName.isEmpty()) {
                return ExecutionResult.error("Menu variable name cannot be empty");
            }
            
            
            VariableManager variableManager = plugin.getServiceRegistry().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager not available");
            }
            
            DataValue menuValue = variableManager.getGlobalVariable(menuVariableName);
            if (menuValue == null || menuValue.getValue() == null) {
                return ExecutionResult.error("Menu variable '" + menuVariableName + "' not found or is null");
            }
            
            if (!(menuValue.getValue() instanceof CreateMenuAction.GUIInventory)) {
                return ExecutionResult.error("Variable '" + menuVariableName + "' does not contain a valid menu");
            }
            
            CreateMenuAction.GUIInventory guiInventory = (CreateMenuAction.GUIInventory) menuValue.getValue();
            
            
            org.bukkit.entity.Player targetPlayer = context.getPlayer();
            if (playerValue != null && playerValue.getValue() instanceof org.bukkit.entity.Player) {
                targetPlayer = (org.bukkit.entity.Player) playerValue.getValue();
            }
            
            if (targetPlayer == null) {
                return ExecutionResult.error("Target player not available");
            }
            
            
            targetPlayer.openInventory(guiInventory.getInventory());
            
            return ExecutionResult.success("Showed menu '" + guiInventory.getTitle() + "' to player " + targetPlayer.getName());
            
        } catch (Exception e) {
            return ExecutionResult.error("Error showing menu: " + e.getMessage());
        }
    }
}