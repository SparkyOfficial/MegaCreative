package com.megacreative.gui.coding.entity_condition;

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
 * 🎆 УЛУЧШЕННЫЙ Графический интерфейс условий сущностей
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 ENHANCED Entity Condition Block GUI
 * 
 * Implements reference system-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 ERWEITERT Entitätsbedingungs-Block-GUI
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
 */
public class EntityConditionBlockGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    // Categories for different types of entity conditions
    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();
    private static final Map<String, Material> CATEGORY_MATERIALS = new HashMap<>();
    
    static {
        // Define category names and their display names
        CATEGORY_NAMES.put("STATE", "🧬 Состояние");
        CATEGORY_NAMES.put("ENVIRONMENT", "🌍 Окружение");
        CATEGORY_NAMES.put("MOVEMENT", "🏃 Движение");
        CATEGORY_NAMES.put("INTERACTION", "🧍 Взаимодействие");
        CATEGORY_NAMES.put("AGE", "🐣 Возраст");
        CATEGORY_NAMES.put("MISC", "🔧 Другое");
        
        // Define materials for category items
        CATEGORY_MATERIALS.put("STATE", Material.REDSTONE);
        CATEGORY_MATERIALS.put("ENVIRONMENT", Material.WATER_BUCKET);
        CATEGORY_MATERIALS.put("MOVEMENT", Material.LEATHER_BOOTS);
        CATEGORY_MATERIALS.put("INTERACTION", Material.SADDLE);
        CATEGORY_MATERIALS.put("AGE", Material.EGG);
        CATEGORY_MATERIALS.put("MISC", Material.ZOMBIE_SPAWN_EGG);
    }
    
    /**
     * Инициализирует графический интерфейс условий сущностей
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     * @param blockMaterial Материал блока для настройки
     */
    public EntityConditionBlockGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        
        // Add null check for service registry
        if (plugin != null && plugin.getServiceRegistry() != null) {
            this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        } else {
            this.blockConfigService = null;
            player.sendMessage("§cBlock configuration service not available!");
        }
        
        // Create inventory with appropriate size
        this.inventory = Bukkit.createInventory(null, 54, "§8Условие сущности: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    /**
     * Получает отображаемое имя блока
     */
    private String getBlockDisplayName() {
        // Get display name from block config service
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        return config != null ? config.getDisplayName() : blockMaterial.name();
    }
    
    /**
     * Настраивает инвентарь графического интерфейса
     */
    private void setupInventory() {
        inventory.clear();
        
        // Add decorative border
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
        
        // Add info item in the center
        ItemStack infoItem = new ItemStack(blockMaterial);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e§l" + getBlockDisplayName());
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Выберите категорию условий сущностей");
        infoLore.add("");
        infoLore.add("§aКликните по категории чтобы");
        infoLore.add("§aпросмотреть доступные условия");
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Add category items
        int slot = 10;
        for (Map.Entry<String, String> category : CATEGORY_NAMES.entrySet()) {
            String categoryKey = category.getKey();
            String categoryName = category.getValue();
            
            ItemStack categoryItem = new ItemStack(CATEGORY_MATERIALS.getOrDefault(categoryKey, Material.PAPER));
            ItemMeta categoryMeta = categoryItem.getItemMeta();
            categoryMeta.setDisplayName("§6" + categoryName);
            
            List<String> categoryLore = new ArrayList<>();
            categoryLore.add("§7Категория: " + categoryKey);
            categoryLore.add("");
            categoryLore.add("§e⚡ Кликните чтобы выбрать");
            categoryMeta.setLore(categoryLore);
            
            categoryItem.setItemMeta(categoryMeta);
            inventory.setItem(slot, categoryItem);
            
            slot += 2; // Space out categories
            if (slot >= 44) break; // Don't go into border area
        }
    }
    
    /**
     * Открывает графический интерфейс для игрока
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Аудио обратная связь при открытии GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        
        // Add visual effects for reference system-style magic
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
            player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 1);
    }
    
    @Override
    /**
     * Получает заголовок графического интерфейса
     * @return Заголовок интерфейса
     */
    public String getGUITitle() {
        return "Entity Condition Block GUI for " + blockMaterial.name();
    }
    
    @Override
    /**
     * Обрабатывает события кликов в инвентаре
     * @param event Событие клика в инвентаре
     */
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true); // Cancel all clicks by default
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        ItemMeta meta = clicked.getItemMeta();
        String displayName = meta.getDisplayName();
        
        // Check if it's a category item
        for (Map.Entry<String, String> category : CATEGORY_NAMES.entrySet()) {
            String categoryName = category.getValue();
            if (displayName.contains(categoryName)) {
                // Open category selection GUI
                openCategorySelectionGUI(category.getKey());
                return;
            }
        }
        
        // Handle other clicks
        List<String> lore = meta.getLore();
        if (lore != null) {
            // Find condition ID in lore
            String conditionId = null;
            for (String line : lore) {
                if (line.startsWith("§8ID: ")) {
                    conditionId = line.substring(5).trim(); // Remove "§8ID: " prefix
                    break;
                }
            }
            
            if (conditionId != null && !conditionId.isEmpty()) {
                selectCondition(conditionId);
            } else {
                player.sendMessage("§eℹ Это заголовок категории. Кликните по условию ниже.");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            }
        }
    }
    
    /**
     * Открывает графический интерфейс выбора условий в категории
     * @param category Категория для отображения
     */
    private void openCategorySelectionGUI(String category) {
        // Create new inventory for category selection
        Inventory categoryInventory = Bukkit.createInventory(null, 54, "§8" + CATEGORY_NAMES.getOrDefault(category, category));
        
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
        
        // Load conditions for this category
        loadConditionsForCategory(categoryInventory, category);
        
        // Open the category inventory
        player.openInventory(categoryInventory);
    }
    
    /**
     * Загружает условия для категории
     * @param inventory Инвентарь для заполнения
     * @param category Категория для загрузки
     */
    private void loadConditionsForCategory(Inventory inventory, String category) {
        // Check if blockConfigService is available
        if (blockConfigService == null) {
            player.sendMessage("§cОшибка: Сервис конфигурации блоков недоступен!");
            return;
        }
        
        // Get available conditions for this block material
        List<String> availableConditions = blockConfigService.getActionsForMaterial(blockMaterial);
        
        // Filter conditions by category
        List<String> categoryConditions = new ArrayList<>();
        for (String conditionId : availableConditions) {
            String conditionCategory = getEntityConditionCategory(conditionId);
            if (category.equals(conditionCategory)) {
                categoryConditions.add(conditionId);
            }
        }
        
        // Create condition items
        int slot = 10;
        for (String conditionId : categoryConditions) {
            if (slot >= 44) break; // Don't go into border area
            
            ItemStack conditionItem = createConditionItem(conditionId);
            inventory.setItem(slot, conditionItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2; // Skip border slots
        }
    }
    
    /**
     * Создает элемент условия
     * @param conditionId ID условия
     * @return ItemStack элемент условия
     */
    private ItemStack createConditionItem(String conditionId) {
        // Create appropriate material for condition type
        Material material = getConditionMaterial(conditionId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§a§l" + getConditionDisplayName(conditionId));
        
        // Set lore with description and category
        List<String> lore = new ArrayList<>();
        lore.add("§7" + getConditionDescription(conditionId));
        lore.add("");
        lore.add("§8⚙️ Категория: " + getConditionCategoryName(conditionId));
        lore.add("");
        lore.add("§e⚡ Кликните чтобы выбрать");
        lore.add("§8ID: " + conditionId);
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Получает отображаемое имя категории для условия
     * @param conditionId ID условия
     * @return Отображаемое имя категории
     */
    private String getConditionCategoryName(String conditionId) {
        String category = getEntityConditionCategory(conditionId);
        return CATEGORY_NAMES.getOrDefault(category, category);
    }
    
    /**
     * Получает категорию для условия сущности
     * @param conditionId ID условия
     * @return Категория условия
     */
    private String getEntityConditionCategory(String conditionId) {
        switch (conditionId.toLowerCase()) {
            case "isentityalive":
            case "isentitydead":
                return "STATE";
            case "isentityonfire":
            case "isentityinwater":
            case "isentityinlava":
                return "ENVIRONMENT";
            case "isentitysneaking":
            case "isentitysprinting":
            case "isentityflying":
                return "MOVEMENT";
            case "hasentitypassenger":
            case "hasentityvehicle":
                return "INTERACTION";
            case "isentityageable":
            case "isentityadult":
                return "AGE";
            default:
                return "MISC";
        }
    }
    
    /**
     * Получает материал для условия
     * @param conditionId ID условия
     * @return Материал для условия
     */
    private Material getConditionMaterial(String conditionId) {
        // Return appropriate materials based on condition type
        switch (conditionId.toLowerCase()) {
            case "isentityalive":
                return Material.REDSTONE;
            case "isentityonfire":
                return Material.FLINT_AND_STEEL;
            case "isentityinwater":
                return Material.WATER_BUCKET;
            case "isentityinlava":
                return Material.LAVA_BUCKET;
            case "isentitysneaking":
                return Material.LEATHER_BOOTS;
            case "hasentitypassenger":
                return Material.SADDLE;
            case "isentityageable":
                return Material.EGG;
            case "isentityadult":
                return Material.CHICKEN_SPAWN_EGG;
            default:
                return Material.STONE;
        }
    }
    
    /**
     * Получает отображаемое имя условия
     * @param conditionId ID условия
     * @return Отображаемое имя условия
     */
    private String getConditionDisplayName(String conditionId) {
        // Return user-friendly names for conditions
        switch (conditionId.toLowerCase()) {
            case "isentityalive": return "Сущность жива";
            case "isentitydead": return "Сущность мертва";
            case "isentityonfire": return "Сущность горит";
            case "isentityinwater": return "Сущность в воде";
            case "isentityinlava": return "Сущность в лаве";
            case "isentitysneaking": return "Сущность приседает";
            case "isentitysprinting": return "Сущность бежит";
            case "isentityflying": return "Сущность летает";
            case "hasentitypassenger": return "Сущность имеет пассажира";
            case "hasentityvehicle": return "Сущность в транспорте";
            case "isentityageable": return "Сущность может взрослеть";
            case "isentityadult": return "Сущность взрослая";
            default: return conditionId;
        }
    }

    /**
     * Получает описание условия
     * @param conditionId ID условия
     * @return Описание условия
     */
    private String getConditionDescription(String conditionId) {
        // Return descriptions for conditions
        switch (conditionId.toLowerCase()) {
            case "isentityalive": return "Проверяет, жива ли сущность";
            case "isentitydead": return "Проверяет, мертва ли сущность";
            case "isentityonfire": return "Проверяет, горит ли сущность";
            case "isentityinwater": return "Проверяет, находится ли сущность в воде";
            case "isentityinlava": return "Проверяет, находится ли сущность в лаве";
            case "isentitysneaking": return "Проверяет, приседает ли сущность";
            case "isentitysprinting": return "Проверяет, бежит ли сущность";
            case "isentityflying": return "Проверяет, летает ли сущность";
            case "hasentitypassenger": return "Проверяет, есть ли у сущности пассажир";
            case "hasentityvehicle": return "Проверяет, находится ли сущность в транспорте";
            case "isentityageable": return "Проверяет, может ли сущность взрослеть";
            case "isentityadult": return "Проверяет, является ли сущность взрослой";
            default: return "Условие сущности " + conditionId;
        }
    }
    
    /**
     * Выбирает условие для блока
     * @param conditionId ID условия
     */
    private void selectCondition(String conditionId) {
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
        
        // Set the condition
        codeBlock.setAction(conditionId);
        
        // Save the world
        var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
        }
        
        // Notify player
        player.sendMessage("§a✓ Условие '" + getConditionDisplayName(conditionId) + "' установлено!");
        player.sendMessage("§eКликните снова по блоку для настройки параметров.");
        
        // Add visual feedback for reference system-style magic
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, 
            player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 1);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        
        // Close this GUI
        player.closeInventory();
    }
    
    @Override
    /**
     * Обрабатывает события закрытия инвентаря
     * @param event Событие закрытия инвентаря
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    /**
     * Выполняет очистку ресурсов при закрытии интерфейса
     */
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}