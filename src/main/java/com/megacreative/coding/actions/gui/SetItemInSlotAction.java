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

/**
 * Action to set an item in a specific slot of a menu
 */
@BlockMeta(
    id = "set_item_in_slot",
    displayName = "Set Item in Slot",
    type = com.megacreative.coding.BlockType.ACTION
)
public class SetItemInSlotAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public SetItemInSlotAction(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue menuVariableValue = block.getParameter("menuVariable");
            DataValue slotValue = block.getParameter("slot");
            DataValue itemValue = block.getParameter("item");
            
            if (menuVariableValue == null || slotValue == null || itemValue == null) {
                return ExecutionResult.error("Missing required parameters: menuVariable, slot, or item");
            }
            
            String menuVariableName = menuVariableValue.asString();
            int slot = slotValue.asNumber().intValue();
            Object itemObj = itemValue.getValue();
            
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
            
            
            if (slot < 0 || slot >= guiInventory.getInventory().getSize()) {
                return ExecutionResult.error("Invalid slot number: " + slot + ". Must be between 0 and " + (guiInventory.getInventory().getSize() - 1));
            }
            
            
            ItemStack itemStack = null;
            if (itemObj instanceof ItemStack) {
                itemStack = (ItemStack) itemObj;
            } else if (itemObj instanceof org.bukkit.Material) {
                itemStack = new org.bukkit.inventory.ItemStack((org.bukkit.Material) itemObj);
            } else {
                return ExecutionResult.error("Invalid item type: " + (itemObj != null ? itemObj.getClass().getSimpleName() : "null"));
            }
            
            
            DataValue titleValue = block.getParameter("title");
            if (titleValue != null && !titleValue.isEmpty()) {
                String title = titleValue.asString();
                if (title != null && !title.isEmpty()) {
                    org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(title);
                        itemStack.setItemMeta(meta);
                    }
                }
            }
            
            
            DataValue loreValue = block.getParameter("lore");
            if (loreValue != null && !loreValue.isEmpty() && loreValue.getValue() instanceof java.util.List) {
                java.util.List<?> loreList = (java.util.List<?>) loreValue.getValue();
                if (!loreList.isEmpty()) {
                    org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        java.util.List<String> lore = new java.util.ArrayList<>();
                        for (Object loreObj : loreList) {
                            if (loreObj != null) {
                                lore.add(loreObj.toString());
                            }
                        }
                        meta.setLore(lore);
                        itemStack.setItemMeta(meta);
                    }
                }
            }
            
            
            guiInventory.setSlotItem(slot, itemStack);
            
            return ExecutionResult.success("Set item in slot " + slot + " of menu '" + guiInventory.getTitle() + "'");
            
        } catch (Exception e) {
            return ExecutionResult.error("Error setting item in slot: " + e.getMessage());
        }
    }
}