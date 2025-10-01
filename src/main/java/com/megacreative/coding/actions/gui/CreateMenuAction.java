package com.megacreative.coding.actions.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.listeners.GUIClickListener;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Action to create a menu/inventory with a specified size and title
 * Stores the created inventory in a variable for later use
 */
@BlockMeta(
    id = "create_menu",
    displayName = "Create Menu",
    type = com.megacreative.coding.BlockType.ACTION
)
public class CreateMenuAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public CreateMenuAction(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue sizeValue = block.getParameter("size");
            DataValue titleValue = block.getParameter("title");
            DataValue variableValue = block.getParameter("variable");
            
            if (sizeValue == null || titleValue == null || variableValue == null) {
                return ExecutionResult.error("Missing required parameters: size, title, or variable");
            }
            
            int size = sizeValue.asNumber().intValue();
            String title = titleValue.asString();
            String variableName = variableValue.asString();
            
            // Validate size (must be multiple of 9, up to 54)
            if (size <= 0 || size > 54 || size % 9 != 0) {
                return ExecutionResult.error("Invalid menu size: " + size + ". Size must be a multiple of 9 and between 9-54.");
            }
            
            if (title == null || title.isEmpty()) {
                return ExecutionResult.error("Menu title cannot be empty");
            }
            
            if (variableName == null || variableName.isEmpty()) {
                return ExecutionResult.error("Variable name cannot be empty");
            }
            
            // Create the inventory
            Inventory inventory = Bukkit.createInventory(null, size, title);
            
            // Register the inventory with the click listener
            GUIClickListener clickListener = plugin.getServiceRegistry().getService(GUIClickListener.class);
            if (clickListener != null) {
                clickListener.registerGUIInventory(inventory, title);
            }
            
            // Store the inventory in a variable
            // We'll use a special wrapper to identify GUI inventories
            GUIInventory guiInventory = new GUIInventory(inventory, title, clickListener);
            
            // Store in variable manager as a global variable so any player can access it
            VariableManager variableManager = plugin.getServiceRegistry().getVariableManager();
            if (variableManager != null) {
                variableManager.setGlobalVariable(variableName, DataValue.fromObject(guiInventory));
            }
            
            return ExecutionResult.success("Created menu '" + title + "' with size " + size + " and stored in global variable '" + variableName + "'");

        } catch (Exception e) {
            return ExecutionResult.error("Error creating menu: " + e.getMessage());
        }
    }
    
    /**
     * Wrapper class for GUI inventories to distinguish them from regular inventories
     */
    public static class GUIInventory {
        private final Inventory inventory;
        private final String title;
        private final UUID id;
        private final GUIClickListener clickListener;
        private final Map<Integer, ItemStack> slotItems = new HashMap<>();
        
        public GUIInventory(Inventory inventory, String title, GUIClickListener clickListener) {
            this.inventory = inventory;
            this.title = title;
            this.clickListener = clickListener;
            this.id = UUID.randomUUID();
        }
        
        public Inventory getInventory() {
            return inventory;
        }
        
        public String getTitle() {
            return title;
        }
        
        public UUID getId() {
            return id;
        }
        
        public GUIClickListener getClickListener() {
            return clickListener;
        }
        
        public void setSlotItem(int slot, ItemStack item) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, item);
                slotItems.put(slot, item);
            }
        }
        
        public ItemStack getSlotItem(int slot) {
            // First check if we have it in our map (for items set via setSlotItem)
            ItemStack item = slotItems.get(slot);
            // If not, check the actual inventory (for items added by player interaction)
            if (item == null && inventory != null) {
                item = inventory.getItem(slot);
            }
            return item;
        }
        
        public Map<Integer, ItemStack> getAllSlotItems() {
            return new HashMap<>(slotItems);
        }
    }
}