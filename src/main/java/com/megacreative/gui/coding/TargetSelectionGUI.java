package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.*;

/**
 * 🎆 Enhanced Target Selection GUI
 * 
 * Implements Reference System-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 Улучшенный графический интерфейс выбора целей
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 Erweiterte Zielauswahl-GUI
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
 */
public class TargetSelectionGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    
    // Target types available in the system with enhanced categorization
    public enum TargetType {
        PLAYER("@p", "§aТекущий игрок", Material.PLAYER_HEAD, "Игрок, который запустил скрипт", "👤 Игроки"),
        ALL_PLAYERS("@a", "§6Все игроки", Material.GOLDEN_HELMET, "Все игроки на сервере", "👥 Группы"),
        RANDOM_PLAYER("@r", "§cСлучайный игрок", Material.COMPASS, "Случайно выбранный игрок", "🎲 Случайные"),
        NEAREST_PLAYER("@n", "§eБлижайший игрок", Material.ENDER_EYE, "Ближайший к блоку игрок", "📍 Расположение"),
        VICTIM("VICTIM", "§4Жертва", Material.SKELETON_SKULL, "Игрок, который пострадал в событии", "⚔️ Боевые"),
        ATTACKER("ATTACKER", "§cАтакующий", Material.DIAMOND_SWORD, "Игрок, который атаковал", "⚔️ Боевые"),
        KILLER("KILLER", "§8Убийца", Material.NETHERITE_SWORD, "Игрок, который убил", "⚔️ Боевые"),
        DEFAULT("DEFAULT", "§7По умолчанию", Material.GRAY_STAINED_GLASS, "Стандартная цель для этого действия", "⚙️ Системные"),
        CUSTOM("CUSTOM", "§eПользовательская", Material.NAME_TAG, "Ввести имя игрока вручную", "✍️ Пользовательские");
        
        private final String selector;
        private final String displayName;
        private final Material icon;
        private final String description;
        private final String category;
        
        TargetType(String selector, String displayName, Material icon, String description, String category) {
            this.selector = selector;
            this.displayName = displayName;
            this.icon = icon;
            this.description = description;
            this.category = category;
        }
        
        public String getSelector() { return selector; }
        public String getDisplayName() { return displayName; }
        public Material getIcon() { return icon; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
    }
    
    /**
     * Initializes target selection GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     * @param blockMaterial Material of block to configure
     */
    public TargetSelectionGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        
        // Create inventory with appropriate size (54 slots for double chest GUI)
        this.inventory = Bukkit.createInventory(null, 54, "§8Выбор цели: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    /**
     * Gets display name for block
     */
    private String getBlockDisplayName() {
        BlockConfigService blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        return config != null ? config.getDisplayName() : blockMaterial.name();
    }
    
    /**
     * Sets up the GUI inventory with enhanced design
     */
    private void setupInventory() {
        inventory.clear();
        
        // Add decorative border with category-specific materials
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        // Fill border slots
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderItem);
            }
        }
        
        // Add title item with enhanced visual design
        ItemStack titleItem = new ItemStack(blockMaterial);
        ItemMeta titleMeta = titleItem.getItemMeta();
        titleMeta.setDisplayName("§e§l" + getBlockDisplayName());
        List<String> titleLore = new ArrayList<>();
        titleLore.add("§7Выберите цель для этого действия");
        titleLore.add("");
        titleLore.add("§aВыбранная цель будет сохранена");
        titleLore.add("§aв блоке как параметр 'target'");
        titleLore.add("");
        titleLore.add("§f✨ Reference system-стиль: универсальные блоки");
        titleLore.add("§fс настройкой через GUI");
        titleMeta.setLore(titleLore);
        titleItem.setItemMeta(titleMeta);
        inventory.setItem(4, titleItem);
        
        // Add category selection first
        loadCategoryOptions();
        
        // Add back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c⬅ Назад");
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Вернуться к предыдущему меню");
        backMeta.setLore(backLore);
        backButton.setItemMeta(backMeta);
        inventory.setItem(49, backButton);
    }
    
    /**
     * Loads category options for target selection
     */
    private void loadCategoryOptions() {
        // Get unique categories
        Set<String> categories = new LinkedHashSet<>();
        for (TargetType targetType : TargetType.values()) {
            categories.add(targetType.getCategory());
        }
        
        // Place category options in a nice grid layout
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int slotIndex = 0;
        
        for (String category : categories) {
            if (slotIndex >= slots.length) break;
            
            ItemStack categoryItem = createCategoryItem(category);
            inventory.setItem(slots[slotIndex], categoryItem);
            slotIndex++;
        }
    }
    
    /**
     * Creates category item
     */
    private ItemStack createCategoryItem(String category) {
        // Determine material based on category
        Material material;
        switch (category) {
            case "👤 Игроки":
                material = Material.PLAYER_HEAD;
                break;
            case "👥 Группы":
                material = Material.GOLDEN_HELMET;
                break;
            case "🎲 Случайные":
                material = Material.COMPASS;
                break;
            case "📍 Расположение":
                material = Material.ENDER_EYE;
                break;
            case "⚔️ Боевые":
                material = Material.DIAMOND_SWORD;
                break;
            case "⚙️ Системные":
                material = Material.GRAY_STAINED_GLASS;
                break;
            case "✍️ Пользовательские":
                material = Material.NAME_TAG;
                break;
            default:
                material = Material.PAPER;
                break;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§l" + category);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Категория целей");
            lore.add("");
            lore.add("§e⚡ Кликните для просмотра");
            lore.add("§eцелей в этой категории");
            lore.add("");
            lore.add("§f✨ Reference system-стиль");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Opens category-specific target selection
     */
    private void openCategoryTargets(String category) {
        // Create new inventory for category targets
        Inventory categoryInventory = Bukkit.createInventory(null, 54, "§8Цели: " + category);
        
        // Add decorative border
        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        // Fill border slots
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                categoryInventory.setItem(i, borderItem);
            }
        }
        
        // Add back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c⬅ Назад");
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Вернуться к выбору категорий");
        backMeta.setLore(backLore);
        backButton.setItemMeta(backMeta);
        categoryInventory.setItem(49, backButton);
        
        // Load targets for this category
        loadTargetsForCategory(categoryInventory, category);
        
        // Open the category inventory
        player.openInventory(categoryInventory);
    }
    
    /**
     * Loads targets for category
     */
    private void loadTargetsForCategory(Inventory inventory, String category) {
        // Place target options in a nice grid layout
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int slotIndex = 0;
        
        for (TargetType targetType : TargetType.values()) {
            if (targetType.getCategory().equals(category)) {
                if (slotIndex >= slots.length) break;
                
                ItemStack targetItem = createTargetItem(targetType);
                inventory.setItem(slots[slotIndex], targetItem);
                slotIndex++;
            }
        }
    }
    
    /**
     * Creates target item with enhanced design
     */
    private ItemStack createTargetItem(TargetType targetType) {
        ItemStack item = new ItemStack(targetType.getIcon());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(targetType.getDisplayName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7" + targetType.getDescription());
            lore.add("");
            
            // 🎆 ENHANCED: Add usage examples and context
            switch (targetType) {
                case PLAYER:
                    lore.add("§a✓ Примеры использования:");
                    lore.add("§7  • Отправка сообщений");
                    lore.add("§7  • Телепортация");
                    lore.add("§7  • Выдача предметов");
                    break;
                case ALL_PLAYERS:
                    lore.add("§a✓ Примеры использования:");
                    lore.add("§7  • Объявления сервера");
                    lore.add("§7  • Глобальные эффекты");
                    break;
                case RANDOM_PLAYER:
                    lore.add("§a✓ Примеры использования:");
                    lore.add("§7  • Случайные награды");
                    lore.add("§7  • Мини-игры");
                    break;
                case NEAREST_PLAYER:
                    lore.add("§a✓ Примеры использования:");
                    lore.add("§7  • Локальные взаимодействия");
                    lore.add("§7  • Проверки близости");
                    break;
                case CUSTOM:
                    lore.add("§a✓ Примеры использования:");
                    lore.add("§7  • Конкретный игрок по имени");
                    lore.add("§7  • Административные команды");
                    break;
            }
            lore.add("");
            
            if (!targetType.getSelector().equals("DEFAULT")) {
                lore.add("§eСелектор: §f" + targetType.getSelector());
            }
            
            lore.add("");
            lore.add("§8Категория: " + targetType.getCategory());
            lore.add("§aКликните для выбора");
            lore.add("§8ID: " + targetType.name());
            lore.add("");
            lore.add("§f✨ Reference system-стиль: универсальные блоки");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Audio feedback when opening GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 0.8f);
        
        // Add visual effects for reference system-style magic
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
            player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 1);
    }
    
    @Override
    /**
     * Gets the GUI title
     * @return Interface title
     */
    public String getGUITitle() {
        return "Target Selection GUI for " + blockMaterial.name();
    }
    
    @Override
    /**
     * Handles inventory click events
     * @param event Inventory click event
     */
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true); // Cancel all clicks by default
        
        int slot = event.getSlot();
        
        // Handle back button
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        ItemMeta meta = clicked.getItemMeta();
        String displayName = meta.getDisplayName();
        
        // Check if it's a category item
        if (displayName.startsWith("§6§l")) {
            String category = displayName.substring(4); // Remove color codes
            openCategoryTargets(category);
            return;
        }
        
        List<String> lore = meta.getLore();
        if (lore == null) return;
        
        // Handle category inventory clicks
        if (inventory.getSize() == 54 && inventory.getItem(49) != null && 
            inventory.getItem(49).hasItemMeta() && 
            inventory.getItem(49).getItemMeta().getDisplayName().equals("§c⬅ Назад")) {
            
            // This is a category inventory, handle back button
            if (slot == 49) {
                // Reopen main inventory
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, this::open, 1L);
                return;
            }
            
            // Find target ID in lore
            String targetId = null;
            for (String line : lore) {
                if (line.startsWith("§8ID: ")) {
                    targetId = line.substring(6); // Remove "§8ID: " prefix
                    break;
                }
            }
            
            if (targetId != null) {
                selectTarget(targetId);
            }
            return;
        }
        
        // Find target ID in lore for main inventory
        String targetId = null;
        for (String line : lore) {
            if (line.startsWith("§8ID: ")) {
                targetId = line.substring(6); // Remove "§8ID: " prefix
                break;
            }
        }
        
        if (targetId != null) {
            selectTarget(targetId);
        }
    }
    
    /**
     * Selects target
     */
    private void selectTarget(String targetId) {
        try {
            TargetType selectedTarget = TargetType.valueOf(targetId);
            
            // Get the code block
            BlockPlacementHandler placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
            if (placementHandler == null) {
                player.sendMessage("§cОшибка: Не удалось получить обработчик блоков");
                return;
            }
            
            CodeBlock codeBlock = placementHandler.getCodeBlock(blockLocation);
            if (codeBlock == null) {
                player.sendMessage("§cОшибка: Блок кода не найден");
                return;
            }
            
            // Set the target parameter
            codeBlock.setParameter("target", selectedTarget.getSelector());
            
            // Save the world
            var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null) {
                plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
            }
            
            // Notify player with enhanced feedback
            player.sendMessage("§a✓ Цель установлена: " + selectedTarget.getDisplayName());
            player.sendMessage("§7» Селектор: §f" + selectedTarget.getSelector());
            player.sendMessage("§e⚡ Теперь выберите действие для блока.");
            
            // Close this GUI
            player.closeInventory();
            
            // Play success sound
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
            
            // Open ActionSelectionGUI next
            openActionSelectionGUI();
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cОшибка: Неизвестный тип цели");
        }
    }
    
    /**
     * Opens ActionSelectionGUI after target selection
     */
    private void openActionSelectionGUI() {
        // Open ActionSelectionGUI after target selection
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ActionSelectionGUI actionGUI = new ActionSelectionGUI(plugin, player, blockLocation, blockMaterial);
            actionGUI.open();
        }, 5L); // Small delay to ensure smooth transition
    }
    
    @Override
    /**
     * Handles inventory close events
     * @param event Inventory close event
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    /**
     * Performs resource cleanup when interface is closed
     */
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}