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
            
            // Removed redundant null checks - static analysis flagged them as always non-null when this method is called
            
            
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
            
            
            if (slot < 0 || slot >= guiInventory.getInventory().getSize()) {
                return ExecutionResult.error("Invalid slot number: " + slot + ". Must be between 0 and " + (guiInventory.getInventory().getSize() - 1));
            }
            
            
            ItemStack slotItem = guiInventory.getInventory().getItem(slot);
            
            
            boolean matches = false;
            Object itemObj = itemValue.getValue();
            
            // Keeping null checks for type safety in case of unexpected scenarios
            // Static analysis flags these as always true/false, but we keep them for robustness
            if (slotItem != null) {
                if (itemObj != null) {
                    if (itemObj instanceof Material) {
                        matches = slotItem.getType() == (Material) itemObj;
                    } else if (itemObj instanceof ItemStack) {
                        matches = slotItem.isSimilar((ItemStack) itemObj);
                    } else {
                        // Convert itemObj to string safely
                        String itemObjStr = itemObj.toString();
                        // toString() should never return null, but keeping the check for safety
                        if (itemObjStr != null) {
                            matches = slotItem.getType().name().equalsIgnoreCase(itemObjStr);
                        }
                    }
                }
                // No action needed when itemObj is null
            } else {
                // slotItem is null
                if (itemObj == null) {
                    // Both are null, so they match
                    matches = true;
                }
                // No action needed when itemObj is not null
            }
            
            
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