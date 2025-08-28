package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.values.types.NumberValue;
import com.megacreative.coding.values.types.TextValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Advanced Create GUI action with DataValue and VariableManager integration
 * Creates dynamic GUIs based on parameters and variables
 * 
 * Parameters:
 * - "title": GUI title (supports variables)
 * - "size": GUI size (9, 18, 27, 36, 45, 54)
 * - "items": List of items to add to GUI
 * - "auto_close": Whether to auto-close after interaction (default: false)
 * 
 * Examples:
 * - Create GUI "Shop" size 27 with items from ${shop_items}
 * - Create GUI "${player} Stats" with dynamic content
 */
public class CreateGuiAction implements BlockAction {
    
    // Static storage for GUI click handlers (in a real implementation, this would be managed better)
    private static final Map<UUID, Map<Integer, CodeBlock>> guiClickHandlers = new HashMap<>();
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();
        
        if (player == null || block == null || variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
        try {
            // Get GUI title
            DataValue titleValue = block.getParameter("title");
            String title = "Custom GUI";
            if (titleValue != null && !titleValue.isEmpty()) {
                DataValue resolvedTitle = resolver.resolve(context, titleValue);
                title = resolvedTitle.asString();
            }
            
            // Get GUI size
            DataValue sizeValue = block.getParameter("size");
            int size = 27; // Default size
            if (sizeValue != null && !sizeValue.isEmpty()) {
                DataValue resolvedSize = resolver.resolve(context, sizeValue);
                if (resolvedSize instanceof NumberValue) {
                    size = ((NumberValue) resolvedSize).asNumber().intValue();
                }
            }
            
            // Validate size
            if (size < 9 || size > 54 || size % 9 != 0) {
                player.sendMessage("§c[CreateGUI] Invalid GUI size: " + size + " (must be 9, 18, 27, 36, 45, or 54)");
                return;
            }
            
            // Create the inventory
            Inventory gui = Bukkit.createInventory(null, size, title);
            
            // Populate GUI with items
            populateGuiItems(context, block, gui, resolver);
            
            // Get auto-close setting
            DataValue autoCloseValue = block.getParameter("auto_close");
            boolean autoClose = false;
            if (autoCloseValue != null && !autoCloseValue.isEmpty()) {
                autoClose = autoCloseValue.asString().equalsIgnoreCase("true");
            }
            
            // Register click handlers for child blocks
            registerClickHandlers(context, player, block, autoClose);
            
            // Open GUI for player
            player.openInventory(gui);
            
            player.sendMessage("§a[CreateGUI] Opened GUI: " + title + " (" + size + " slots)");
            
        } catch (Exception e) {
            player.sendMessage("§c[CreateGUI] Error creating GUI: " + e.getMessage());
            context.getPlugin().getLogger().warning("CreateGUI execution error: " + e.getMessage());
        }
    }
    
    /**
     * Populates GUI with items from parameters or variables
     */
    private void populateGuiItems(ExecutionContext context, CodeBlock block, Inventory gui, ParameterResolver resolver) {
        // Try to get items from "items" parameter (ListValue)
        DataValue itemsValue = block.getParameter("items");
        if (itemsValue != null && !itemsValue.isEmpty()) {
            DataValue resolvedItems = resolver.resolve(context, itemsValue);
            if (resolvedItems instanceof ListValue) {
                populateFromList(gui, (ListValue) resolvedItems);
                return;
            }
        }
        
        // Try to get items from individual slot parameters
        populateFromSlotParameters(context, block, gui, resolver);
        
        // Fallback: Add some default items if no items specified
        if (isEmpty(gui)) {
            addDefaultItems(gui);
        }
    }
    
    /**
     * Populates GUI from a ListValue containing item descriptions
     */
    private void populateFromList(Inventory gui, ListValue itemsList) {
        for (int i = 0; i < itemsList.size() && i < gui.getSize(); i++) {
            DataValue itemValue = itemsList.get(i);
            ItemStack item = createItemFromValue(itemValue);
            if (item != null) {
                gui.setItem(i, item);
            }
        }
    }
    
    /**
     * Populates GUI from individual slot parameters (slot_0, slot_1, etc.)
     */
    private void populateFromSlotParameters(ExecutionContext context, CodeBlock block, Inventory gui, ParameterResolver resolver) {
        for (int slot = 0; slot < gui.getSize(); slot++) {
            DataValue slotValue = block.getParameter("slot_" + slot);
            if (slotValue != null && !slotValue.isEmpty()) {
                DataValue resolvedSlot = resolver.resolve(context, slotValue);
                ItemStack item = createItemFromValue(resolvedSlot);
                if (item != null) {
                    gui.setItem(slot, item);
                }
            }
        }
    }
    
    /**
     * Creates an ItemStack from a DataValue
     */
    private ItemStack createItemFromValue(DataValue value) {
        if (value == null || value.isEmpty()) return null;
        
        String valueStr = value.asString();
        
        // Parse format: "MATERIAL:amount:name:lore1|lore2"
        String[] parts = valueStr.split(":", 4);
        
        // Get material
        Material material;
        try {
            material = Material.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.STONE; // Default material
        }
        
        // Get amount
        int amount = 1;
        if (parts.length > 1) {
            try {
                amount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                // Use default amount
            }
        }
        
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            if (parts.length > 2 && !parts[2].isEmpty()) {
                meta.setDisplayName("§r" + parts[2]);
            }
            
            // Set lore
            if (parts.length > 3 && !parts[3].isEmpty()) {
                String[] loreLines = parts[3].split("\\|");
                List<String> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add("§7" + line);
                }
                meta.setLore(lore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Adds default items to empty GUI
     */
    private void addDefaultItems(Inventory gui) {
        // Add some filler items
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName("§8");
            filler.setItemMeta(fillerMeta);
        }
        
        // Fill border slots
        for (int i = 0; i < gui.getSize(); i++) {
            if (i < 9 || i >= gui.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, filler);
            }
        }
        
        // Add close button
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cClose");
            closeMeta.setLore(List.of("§7Click to close this GUI"));
            closeButton.setItemMeta(closeMeta);
        }
        
        gui.setItem(gui.getSize() - 1, closeButton);
    }
    
    /**
     * Registers click handlers for child blocks
     */
    private void registerClickHandlers(ExecutionContext context, Player player, CodeBlock block, boolean autoClose) {
        UUID playerId = player.getUniqueId();
        Map<Integer, CodeBlock> handlers = new HashMap<>();
        
        // Register each child block as a click handler for specific slots
        int slotIndex = 0;
        for (CodeBlock childBlock : block.getChildren()) {
            handlers.put(slotIndex++, childBlock);
        }
        
        guiClickHandlers.put(playerId, handlers);
        
        // Auto-remove handlers after timeout (prevent memory leaks)
        Bukkit.getScheduler().runTaskLater(
            context.getPlugin(),
            () -> guiClickHandlers.remove(playerId),
            20 * 300 // 5 minutes
        );
    }
    
    /**
     * Checks if inventory is empty
     */
    private boolean isEmpty(Inventory gui) {
        for (ItemStack item : gui.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets click handlers for a player (for use by GUI event listeners)
     */
    public static Map<Integer, CodeBlock> getClickHandlers(UUID playerId) {
        return guiClickHandlers.get(playerId);
    }
    
    /**
     * Removes click handlers for a player
     */
    public static void removeClickHandlers(UUID playerId) {
        guiClickHandlers.remove(playerId);
    }
}