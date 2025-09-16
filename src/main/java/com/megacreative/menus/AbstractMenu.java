package com.megacreative.menus;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.megacreative.utils.ItemUtils.createItem;

/**
 * Abstract base class for all menus in MegaCreative
 * Implements hierarchical menu system with categories like OpenCreative
 */
public abstract class AbstractMenu implements InventoryMenu {
    
    protected final long creationTime;
    protected Inventory inventory;
    protected String title;
    protected int rows;
    
    // Standard items used across menus
    protected final ItemStack AIR_ITEM = new ItemStack(Material.AIR);
    protected final ItemStack DECORATION_ITEM = createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ");
    protected final ItemStack CATEGORY_SEPARATOR = createItem(Material.ORANGE_STAINED_GLASS_PANE, 1, " ");
    
    // Slots where content can be placed (standard 6-row inventory pattern)
    protected final int[] CONTENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16, 
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    public AbstractMenu(int rows, String title) {
        this.rows = Math.max(1, Math.min(6, rows)); // Clamp between 1-6 rows
        this.title = title;
        this.creationTime = System.currentTimeMillis();
    }
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(this, rows * 9, Component.text(this.title));
        }
        return inventory;
    }
    
    @Override
    public void open(@NotNull Player player) {
        MenusManager.getInstance().registerMenu(this);
        try {
            inventory = getInventory();
            fillItems(player);
            player.openInventory(inventory);
        } catch (Exception e) {
            player.sendMessage("§cFailed to open menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Fill the inventory with items
     * @param player The player viewing the menu
     */
    public abstract void fillItems(Player player);
    
    /**
     * Handle inventory click events
     * @param event The click event
     */
    public abstract void onClick(@NotNull InventoryClickEvent event);
    
    /**
     * Handle inventory open events
     * @param event The open event
     */
    public abstract void onOpen(@NotNull InventoryOpenEvent event);
    
    /**
     * Set an item in the inventory
     * @param slot The slot to set the item in
     * @param item The item to set
     */
    protected void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < getInventory().getSize()) {
            getInventory().setItem(slot, item != null ? item : AIR_ITEM);
        }
    }
    
    /**
     * Set the same item in multiple slots
     * @param item The item to set
     * @param slots The slots to set the item in
     */
    protected void setItem(ItemStack item, int... slots) {
        for (int slot : slots) {
            setItem(slot, item);
        }
    }
    
    /**
     * Get an item from the inventory
     * @param slot The slot to get the item from
     * @return The item in the slot, or AIR_ITEM if empty
     */
    protected @NotNull ItemStack getItem(int slot) {
        if (slot < 0 || slot >= getInventory().getSize()) {
            return AIR_ITEM.clone();
        }
        ItemStack item = getInventory().getItem(slot);
        return item == null ? AIR_ITEM.clone() : item;
    }
    
    /**
     * Check if an item is not empty (not air)
     * @param item The item to check
     * @return true if the item is not empty
     */
    protected boolean isNotEmpty(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }
    
    /**
     * Create a category header item
     * @param categoryName The name of the category
     * @param itemCount The number of items in the category
     * @return The category header item
     */
    protected ItemStack createCategoryHeader(String categoryName, int itemCount) {
        ItemStack item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§l" + categoryName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Items: " + itemCount);
        lore.add("§8Category");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create a decorative item for menu borders
     * @return The decorative item
     */
    protected ItemStack createDecorationItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Fill the border of the inventory with decorative items
     */
    protected void fillBorder() {
        ItemStack decoration = createDecorationItem();
        
        // Fill top and bottom rows
        for (int i = 0; i < 9; i++) {
            setItem(i, decoration); // Top row
            if (rows > 1) {
                setItem((rows - 1) * 9 + i, decoration); // Bottom row
            }
        }
        
        // Fill sides
        for (int i = 1; i < rows - 1; i++) {
            setItem(i * 9, decoration); // Left side
            setItem(i * 9 + 8, decoration); // Right side
        }
    }
    
    @Override
    public void destroy() {
        MenusManager.getInstance().unregisterMenu(this);
    }
}