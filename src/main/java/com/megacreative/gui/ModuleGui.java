package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

/**
 * A flexible GUI framework that allows creating dynamic menus with categories and items.
 * This system is inspired by the ModuleGui pattern used in other creative coding plugins.
 * 
 * Features:
 * - Category-based navigation
 * - Dynamic item creation
 * - Callback-based item handling
 * - Visual styling with decorative elements
 * - Easy integration with existing GUIManager
 */
public class ModuleGui implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final String title;
    private final int size;
    private final Inventory inventory;
    private final GUIManager guiManager;
    
    // Store items and their click handlers
    private final Map<Integer, Consumer<ModuleGuiClickEvent>> clickHandlers = new HashMap<>();
    
    // Category system
    private final Map<String, Category> categories = new LinkedHashMap<>();
    private String currentCategory = null;
    
    /**
     * Creates a new ModuleGui
     * @param plugin The plugin instance
     * @param player The player who will use this GUI
     * @param title The title of the GUI
     * @param rows The number of rows (1-6)
     */
    public ModuleGui(MegaCreative plugin, Player player, String title, int rows) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.size = Math.max(9, Math.min(54, rows * 9)); // Clamp between 9 and 54
        this.inventory = Bukkit.createInventory(null, this.size, title);
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
    }
    
    /**
     * Adds a category to the GUI
     * @param id The unique ID of the category
     * @param displayName The display name of the category
     * @param material The material to use for the category icon
     * @param description The description of the category
     * @return The created category
     */
    public Category addCategory(String id, String displayName, Material material, String description) {
        Category category = new Category(id, displayName, material, description);
        categories.put(id, category);
        return category;
    }
    
    /**
     * Gets a category by ID
     * @param id The ID of the category
     * @return The category, or null if not found
     */
    public Category getCategory(String id) {
        return categories.get(id);
    }
    
    /**
     * Gets all categories
     * @return Collection of all categories
     */
    public Collection<Category> getCategories() {
        return categories.values();
    }
    
    /**
     * Sets the current category and updates the GUI
     * @param categoryId The ID of the category to show
     */
    public void setCurrentCategory(String categoryId) {
        this.currentCategory = categoryId;
        updateInventory();
    }
    
    /**
     * Updates the inventory contents based on the current state
     */
    public void updateInventory() {
        inventory.clear();
        clickHandlers.clear();
        
        if (currentCategory == null) {
            // Show category selection
            showCategories();
        } else {
            // Show items in category
            showCategoryItems(currentCategory);
        }
    }
    
    /**
     * Shows the category selection screen
     */
    private void showCategories() {
        // Add decorative border
        addBorder();
        
        // Add title item
        ItemStack titleItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta titleMeta = titleItem.getItemMeta();
        titleMeta.setDisplayName("§6" + title);
        List<String> titleLore = new ArrayList<>();
        titleLore.add("§7Выберите категорию");
        titleLore.add("");
        titleLore.add("§eКликните по категории чтобы");
        titleLore.add("§eпросмотреть доступные действия");
        titleMeta.setLore(titleLore);
        titleItem.setItemMeta(titleMeta);
        inventory.setItem(4, titleItem);
        
        // Add category items
        int slot = 10;
        for (Category category : categories.values()) {
            if (slot >= size - 9) break; // Don't go into border area
            
            ItemStack categoryItem = new ItemStack(category.getMaterial());
            ItemMeta categoryMeta = categoryItem.getItemMeta();
            categoryMeta.setDisplayName("§6" + category.getDisplayName());
            
            List<String> categoryLore = new ArrayList<>();
            categoryLore.add("§7" + category.getDescription());
            categoryLore.add("");
            categoryLore.add("§e⚡ Кликните чтобы выбрать");
            categoryMeta.setLore(categoryLore);
            
            categoryItem.setItemMeta(categoryMeta);
            inventory.setItem(slot, categoryItem);
            
            // Register click handler
            final String categoryId = category.getId();
            clickHandlers.put(slot, event -> setCurrentCategory(categoryId));
            
            slot += 2; // Space out categories
        }
    }
    
    /**
     * Shows items in a specific category
     */
    private void showCategoryItems(String categoryId) {
        Category category = categories.get(categoryId);
        if (category == null) return;
        
        // Add decorative border
        addBorder();
        
        // Add back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§c← Назад");
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Вернуться к выбору категорий");
        backMeta.setLore(backLore);
        backItem.setItemMeta(backMeta);
        inventory.setItem(0, backItem);
        clickHandlers.put(0, event -> setCurrentCategory(null));
        
        // Add category title
        ItemStack categoryItem = new ItemStack(category.getMaterial());
        ItemMeta categoryMeta = categoryItem.getItemMeta();
        categoryMeta.setDisplayName("§6" + category.getDisplayName());
        List<String> categoryLore = new ArrayList<>();
        categoryLore.add("§7" + category.getDescription());
        categoryLore.add("");
        categoryLore.add("§eВыберите действие из категории");
        categoryMeta.setLore(categoryLore);
        categoryItem.setItemMeta(categoryMeta);
        inventory.setItem(4, categoryItem);
        
        // Add category items
        int slot = 9;
        for (GuiItem item : category.getItems()) {
            if (slot >= size) break;
            
            inventory.setItem(slot, item.getItemStack());
            if (item.getClickHandler() != null) {
                clickHandlers.put(slot, item.getClickHandler());
            }
            
            slot++;
        }
    }
    
    /**
     * Adds a decorative border to the inventory
     */
    private void addBorder() {
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        // Fill border slots
        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderItem);
            }
        }
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        updateInventory();
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Play sound effect
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
    }
    
    @Override
    public String getGUITitle() {
        return title;
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < size) {
            Consumer<ModuleGuiClickEvent> handler = clickHandlers.get(slot);
            if (handler != null) {
                ModuleGuiClickEvent clickEvent = new ModuleGuiClickEvent(this, event.getWhoClicked(), slot, event.getCurrentItem());
                handler.accept(clickEvent);
            }
        }
    }
    
    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Clean up resources if needed
    }
    
    /**
     * Gets the plugin instance
     * @return The plugin
     */
    public MegaCreative getPlugin() {
        return plugin;
    }
    
    /**
     * Gets the player using this GUI
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Gets the inventory
     * @return The inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Represents a category in the GUI
     */
    public static class Category {
        private final String id;
        private final String displayName;
        private final Material material;
        private final String description;
        private final List<GuiItem> items = new ArrayList<>();
        
        public Category(String id, String displayName, Material material, String description) {
            this.id = id;
            this.displayName = displayName;
            this.material = material;
            this.description = description;
        }
        
        /**
         * Adds an item to this category
         * @param item The item to add
         * @return This category for chaining
         */
        public Category addItem(GuiItem item) {
            items.add(item);
            return this;
        }
        
        /**
         * Creates and adds a new item to this category
         * @param material The material for the item
         * @param displayName The display name
         * @param lore The lore/description
         * @param clickHandler The click handler
         * @return The created item
         */
        public GuiItem addItem(Material material, String displayName, List<String> lore, Consumer<ModuleGuiClickEvent> clickHandler) {
            GuiItem item = new GuiItem(material, displayName, lore, clickHandler);
            items.add(item);
            return item;
        }
        
        // Getters
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public Material getMaterial() { return material; }
        public String getDescription() { return description; }
        public List<GuiItem> getItems() { return new ArrayList<>(items); }
    }
    
    /**
     * Represents an item in the GUI
     */
    public static class GuiItem {
        private final ItemStack itemStack;
        private final Consumer<ModuleGuiClickEvent> clickHandler;
        
        public GuiItem(ItemStack itemStack, Consumer<ModuleGuiClickEvent> clickHandler) {
            this.itemStack = itemStack;
            this.clickHandler = clickHandler;
        }
        
        public GuiItem(Material material, String displayName, List<String> lore, Consumer<ModuleGuiClickEvent> clickHandler) {
            this.itemStack = new ItemStack(material);
            ItemMeta meta = this.itemStack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(displayName);
                meta.setLore(lore);
                this.itemStack.setItemMeta(meta);
            }
            this.clickHandler = clickHandler;
        }
        
        // Getters
        public ItemStack getItemStack() { return itemStack; }
        public Consumer<ModuleGuiClickEvent> getClickHandler() { return clickHandler; }
    }
    
    /**
     * Represents a click event in the ModuleGui
     */
    public static class ModuleGuiClickEvent {
        private final ModuleGui gui;
        private final org.bukkit.entity.HumanEntity whoClicked;
        private final int slot;
        private final ItemStack currentItem;
        
        public ModuleGuiClickEvent(ModuleGui gui, org.bukkit.entity.HumanEntity whoClicked, int slot, ItemStack currentItem) {
            this.gui = gui;
            this.whoClicked = whoClicked;
            this.slot = slot;
            this.currentItem = currentItem;
        }
        
        // Getters
        public ModuleGui getGui() { return gui; }
        public org.bukkit.entity.HumanEntity getWhoClicked() { return whoClicked; }
        public Player getPlayer() { return (Player) whoClicked; }
        public int getSlot() { return slot; }
        public ItemStack getCurrentItem() { return currentItem; }
    }
}