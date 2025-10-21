package com.megacreative.menus;

import com.megacreative.utils.LogUtils;
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
 * 
 * Абстрактный базовый класс для всех меню в MegaCreative
 * Реализует иерархическую систему меню с категориями, как в OpenCreative
 * 
 * @author Андрій Будильников
 */
public abstract class AbstractMenu implements InventoryMenu {
    
    protected final long creationTime;
    protected Inventory inventory;
    protected String title;
    protected int rows;
    
    
    protected final ItemStack AIR_ITEM = new ItemStack(Material.AIR);
    protected final ItemStack DECORATION_ITEM = createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ");
    protected final ItemStack CATEGORY_SEPARATOR = createItem(Material.ORANGE_STAINED_GLASS_PANE, 1, " ");
    
    
    protected final int[] CONTENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16, 
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    public AbstractMenu(int rows, String title) {
        this.rows = Math.max(1, Math.min(6, rows)); 
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
            com.megacreative.utils.LogUtils.error("Failed to open menu", e);
        }
    }
    
    /**
     * Fill the inventory with items
     * @param player The player viewing the menu
     * 
     * Заполняет инвентарь предметами
     * @param player Игрок, просматривающий меню
     */
    public abstract void fillItems(Player player);
    
    /**
     * Handle inventory click events
     * @param event The click event
     * 
     * Обрабатывает события кликов в инвентаре
     * @param event Событие клика
     */
    public abstract void onClick(@NotNull InventoryClickEvent event);
    
    /**
     * Handle inventory open events
     * @param event The open event
     * 
     * Обрабатывает события открытия инвентаря
     * @param event Событие открытия
     */
    public abstract void onOpen(@NotNull InventoryOpenEvent event);
    
    /**
     * Set an item in the inventory
     * @param slot The slot to set the item in
     * @param item The item to set
     * 
     * Устанавливает предмет в инвентаре
     * @param slot Слот для установки предмета
     * @param item Предмет для установки
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
     * 
     * Устанавливает один и тот же предмет в нескольких слотах
     * @param item Предмет для установки
     * @param slots Слоты для установки предмета
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
     * 
     * Получает предмет из инвентаря
     * @param slot Слот для получения предмета
     * @return Предмет в слоте или AIR_ITEM, если пусто
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
     * 
     * Проверяет, не является ли предмет пустым (не воздухом)
     * @param item Предмет для проверки
     * @return true, если предмет не пустой
     */
    protected boolean isNotEmpty(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }
    
    /**
     * Create a category header item
     * @param categoryName The name of the category
     * @param itemCount The number of items in the category
     * @return The category header item
     * 
     * Создает заголовок категории
     * @param categoryName Название категории
     * @param itemCount Количество предметов в категории
     * @return Заголовок категории
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
     * 
     * Создает декоративный предмет для границ меню
     * @return Декоративный предмет
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
     * 
     * Заполняет границы инвентаря декоративными предметами
     */
    protected void fillBorder() {
        ItemStack decoration = createDecorationItem();
        
        
        for (int i = 0; i < 9; i++) {
            setItem(i, decoration); 
            if (rows > 1) {
                setItem((rows - 1) * 9 + i, decoration); 
            }
        }
        
        
        for (int i = 1; i < rows - 1; i++) {
            setItem(i * 9, decoration); 
            setItem(i * 9 + 8, decoration); 
        }
    }
    
    // Removed identical method to super method
    // The destroy() method was identical to the one in the super interface,
    // so it has been removed to eliminate redundancy
    // Удален идентичный метод из суперинтерфейса
    // Метод destroy() был идентичен методу в суперинтерфейсе,
    // поэтому он был удален для устранения избыточности
}