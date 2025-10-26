package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener for handling coding GUI interactions
 * 
 * @author Андрій Будильников
 */
public class CodingGUIListener implements Listener {
    private final MegaCreative plugin;
    private final Map<UUID, CategorySelectionGUI> categoryGUIs = new HashMap<>();
    private final Map<UUID, ActionSelectionGUI> actionGUIs = new HashMap<>();
    private final SignUpdater signUpdater;
    
    public CodingGUIListener(MegaCreative plugin) {
        this.plugin = plugin;
        this.signUpdater = new SignUpdater(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Handles inventory click events
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        Inventory inventory = event.getInventory();
        if (inventory == null) {
            return;
        }
        
        String inventoryTitle = event.getView().getTitle();
        
        // Handle category selection GUI
        if (inventoryTitle.equals(ChatColor.DARK_PURPLE + "Выбор категории")) {
            handleCategorySelectionClick(event, player, inventory);
            return;
        }
        
        // Handle action selection GUI
        if (inventoryTitle.startsWith(ChatColor.DARK_PURPLE + "Выбор действия")) {
            handleActionSelectionClick(event, player, inventory);
            return;
        }
    }
    
    /**
     * Handles clicks in the category selection GUI
     */
    private void handleCategorySelectionClick(InventoryClickEvent event, Player player, Inventory inventory) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }
        
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        
        // Handle close button
        if (displayName.equals("Закрыть")) {
            player.closeInventory();
            return;
        }
        
        // Handle category selection
        CategorySelectionGUI categoryGUI = categoryGUIs.get(player.getUniqueId());
        if (categoryGUI != null) {
            CodeBlock codeBlock = categoryGUI.getCodeBlock();
            
            // Open action selection GUI for the selected category
            ActionSelectionGUI actionGUI = new ActionSelectionGUI(
                plugin, player, codeBlock, displayName);
            actionGUIs.put(player.getUniqueId(), actionGUI);
            actionGUI.open();
        }
    }
    
    /**
     * Handles clicks in the action selection GUI
     */
    private void handleActionSelectionClick(InventoryClickEvent event, Player player, Inventory inventory) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }
        
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        
        // Handle navigation buttons
        if (displayName.equals("Назад")) {
            // Go back to category selection
            CategorySelectionGUI categoryGUI = categoryGUIs.get(player.getUniqueId());
            if (categoryGUI != null) {
                categoryGUI.open();
            }
            return;
        }
        
        if (displayName.equals("Закрыть")) {
            player.closeInventory();
            return;
        }
        
        // Handle action selection
        ActionSelectionGUI actionGUI = actionGUIs.get(player.getUniqueId());
        if (actionGUI != null) {
            // Find the action ID from the item lore
            if (meta.hasLore()) {
                for (String loreLine : meta.getLore()) {
                    if (loreLine.startsWith(ChatColor.YELLOW + "ID: " + ChatColor.WHITE)) {
                        String actionId = loreLine.substring((ChatColor.YELLOW + "ID: " + ChatColor.WHITE).length());
                        
                        // Set the action on the code block
                        CodeBlock codeBlock = actionGUI.getCodeBlock();
                        codeBlock.setAction(actionId);
                        
                        // Find the location of the code block
                        Location blockLocation = findBlockLocation(codeBlock);
                        if (blockLocation != null) {
                            // Update the sign
                            signUpdater.updateSign(blockLocation, codeBlock);
                            
                            // Close the inventory
                            player.closeInventory();
                            
                            // Send confirmation message
                            player.sendMessage(ChatColor.GREEN + "Действие '" + actionId + "' установлено!");
                            player.sendMessage(ChatColor.YELLOW + "Табличка обновлена!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Ошибка: Не удалось найти расположение блока!");
                        }
                        
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Finds the location of a code block in the world
     * 
     * @param targetBlock The code block to find
     * @return The location of the block, or null if not found
     */
    private Location findBlockLocation(CodeBlock targetBlock) {
        // Get all code blocks from the placement handler
        Map<Location, CodeBlock> allBlocks = plugin.getServiceRegistry()
            .getBlockPlacementHandler().getBlockCodeBlocks();
        
        // Search for the target block
        for (Map.Entry<Location, CodeBlock> entry : allBlocks.entrySet()) {
            if (entry.getValue().getId().equals(targetBlock.getId())) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    /**
     * Handles inventory close events
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        // Clean up GUI references when inventory is closed
        UUID playerId = player.getUniqueId();
        categoryGUIs.remove(playerId);
        actionGUIs.remove(playerId);
    }
    
    /**
     * Opens the category selection GUI for a player and code block
     */
    public void openCategorySelectionGUI(Player player, CodeBlock codeBlock) {
        CategorySelectionGUI categoryGUI = new CategorySelectionGUI(plugin, player, codeBlock);
        categoryGUIs.put(player.getUniqueId(), categoryGUI);
        categoryGUI.open();
    }
    
    /**
     * Opens the action selection GUI for a player, code block, and category
     */
    public void openActionSelectionGUI(Player player, CodeBlock codeBlock, String category) {
        ActionSelectionGUI actionGUI = new ActionSelectionGUI(plugin, player, codeBlock, category);
        actionGUIs.put(player.getUniqueId(), actionGUI);
        actionGUI.open();
    }
}