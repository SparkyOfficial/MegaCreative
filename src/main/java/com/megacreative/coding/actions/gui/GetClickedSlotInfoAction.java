package com.megacreative.coding.actions.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.events.GUIClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Action to get information about the clicked slot in a GUI
 * This action should be used in response to a GUI click event
 */
@BlockMeta(
    id = "get_clicked_slot_info",
    displayName = "Get Clicked Slot Info",
    type = com.megacreative.coding.BlockType.ACTION
)
public class GetClickedSlotInfoAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public GetClickedSlotInfoAction(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue variableValue = block.getParameter("variable");
            
            if (variableValue == null) {
                return ExecutionResult.error("Missing required parameter: variable");
            }
            
            String variableName = variableValue.asString();
            if (variableName == null || variableName.isEmpty()) {
                return ExecutionResult.error("Variable name cannot be empty");
            }
            
            // Get the GUI click event from the context
            Object eventObj = context.getEvent();
            if (!(eventObj instanceof GUIClickEvent)) {
                return ExecutionResult.error("This action can only be used in a GUI click event context");
            }
            
            GUIClickEvent guiClickEvent = (GUIClickEvent) eventObj;
            
            // Create a map with the slot information
            Map<String, Object> slotInfo = new HashMap<>();
            slotInfo.put("slot", guiClickEvent.getSlot());
            slotInfo.put("clickType", guiClickEvent.getClickType());
            slotInfo.put("menuTitle", guiClickEvent.getMenuTitle());
            slotInfo.put("player", guiClickEvent.getPlayer().getName());
            
            // Add item information if there is an item
            ItemStack clickedItem = guiClickEvent.getItem();
            if (clickedItem != null) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("type", clickedItem.getType().name());
                itemInfo.put("amount", clickedItem.getAmount());
                
                if (clickedItem.hasItemMeta()) {
                    // Add display name if present
                    if (clickedItem.getItemMeta().hasDisplayName()) {
                        itemInfo.put("displayName", clickedItem.getItemMeta().getDisplayName());
                    }
                    
                    // Add lore if present
                    if (clickedItem.getItemMeta().hasLore()) {
                        itemInfo.put("lore", clickedItem.getItemMeta().getLore());
                    }
                }
                
                slotInfo.put("item", itemInfo);
            }
            
            // Store the slot information in a variable
            VariableManager variableManager = plugin.getServiceRegistry().getVariableManager();
            if (variableManager != null && context.getPlayer() != null) {
                variableManager.setPlayerVariable(
                    context.getPlayer().getUniqueId(), 
                    variableName, 
                    DataValue.fromObject(slotInfo)
                );
            }
            
            return ExecutionResult.success("Stored clicked slot information in variable '" + variableName + "'");
            
        } catch (Exception e) {
            return ExecutionResult.error("Error getting clicked slot info: " + e.getMessage());
        }
    }
}