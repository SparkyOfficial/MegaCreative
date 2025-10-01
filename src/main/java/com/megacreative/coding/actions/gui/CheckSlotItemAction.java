package com.megacreative.coding.actions.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

/**
 * Action to check if a slot in a menu has a specific item
 */
@BlockMeta(
    id = "check_slot_item",
    displayName = "Check Slot Item",
    type = com.megacreative.coding.BlockType.ACTION
)
public class CheckSlotItemAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public CheckSlotItemAction(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue menuVariableValue = block.getParameter("menuVariable");
            DataValue slotValue = block.getParameter("slot");
            DataValue itemValue = block.getParameter("item");
            DataValue resultVariableValue = block.getParameter("resultVariable");
            
            if (menuVariableValue == null || slotValue == null || itemValue == null || resultVariableValue == null) {
                return ExecutionResult.error("Missing required parameters: menuVariable, slot, item, or resultVariable");
            }
            
            String menuVariableName = menuVariableValue.asString();
            int slot = slotValue.asNumber().intValue();
            String resultVariableName = resultVariableValue.asString();
            
            if (menuVariableName == null || menuVariableName.isEmpty()) {
                return ExecutionResult.error("Menu variable name cannot be empty");
            }
            
            if (resultVariableName == null || resultVariableName.isEmpty()) {
                return ExecutionResult.error("Result variable name cannot be empty");
            }
            
            // Get the GUI inventory from the variable
            VariableManager variableManager = plugin.getServiceRegistry().getVariableManager();
            if (variableManager == null || context.getPlayer() == null) {
                return ExecutionResult.error("Variable manager or player not available");
            }
            
            DataValue menuValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), menuVariableName);
            if (menuValue == null || menuValue.getValue() == null) {
                return ExecutionResult.error("Menu variable '" + menuVariableName + "' not found or is null");
            }
            
            if (!(menuValue.getValue() instanceof CreateMenuAction.GUIInventory)) {
                return ExecutionResult.error("Variable '" + menuVariableName + "' does not contain a valid menu");
            }
            
            CreateMenuAction.GUIInventory guiInventory = (CreateMenuAction.GUIInventory) menuValue.getValue();
            
            // Validate slot
            if (slot < 0 || slot >= guiInventory.getInventory().getSize()) {
                return ExecutionResult.error("Invalid slot number: " + slot + ". Must be between 0 and " + (guiInventory.getInventory().getSize() - 1));
            }
            
            // Get the item in the slot
            ItemStack slotItem = guiInventory.getSlotItem(slot);
            
            // Check if the item matches
            boolean matches = false;
            Object itemObj = itemValue.getValue();
            
            if (slotItem != null && itemObj != null) {
                if (itemObj instanceof Material) {
                    matches = slotItem.getType() == (Material) itemObj;
                } else if (itemObj instanceof ItemStack) {
                    matches = slotItem.isSimilar((ItemStack) itemObj);
                } else {
                    // Compare by material name
                    matches = slotItem.getType().name().equalsIgnoreCase(itemObj.toString());
                }
            } else if (slotItem == null && itemObj == null) {
                // Both are null, so they match
                matches = true;
            }
            
            // Store the result in a variable
            variableManager.setPlayerVariable(
                context.getPlayer().getUniqueId(), 
                resultVariableName, 
                DataValue.fromObject(matches)
            );
            
            return ExecutionResult.success("Checked slot " + slot + " item. Result: " + matches + " (stored in variable '" + resultVariableName + "')");
            
        } catch (Exception e) {
            return ExecutionResult.error("Error checking slot item: " + e.getMessage());
        }
    }
}