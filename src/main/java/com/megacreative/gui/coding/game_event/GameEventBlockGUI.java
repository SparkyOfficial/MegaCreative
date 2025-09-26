package com.megacreative.gui.coding.game_event;

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
 * 🎆 УЛУЧШЕННЫЙ Графический интерфейс игровых событий
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 ENHANCED Game Event Block GUI
 * 
 * Implements reference system-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 ERWEITERT Spielereignis-Block-GUI
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
 */
public class GameEventBlockGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    // Categories for different types of game events
    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();
    private static final Map<String, Material> CATEGORY_MATERIALS = new HashMap<>();
    
    static {
        // Define category names and their display names
        CATEGORY_NAMES.put("SERVER", "🖥️ Сервер");
        CATEGORY_NAMES.put("WORLD", "🌍 Мир");
        CATEGORY_NAMES.put("BLOCK", "🧱 Блоки");
        CATEGORY_NAMES.put("ENTITY", "🧟 Существа");
        CATEGORY_NAMES.put("REDSTONE", "⚙️ Редстоун");
        CATEGORY_NAMES.put("MISC", "🔧 Другое");
        
        // Define materials for category items
        CATEGORY_MATERIALS.put("SERVER", Material.COMMAND_BLOCK);
        CATEGORY_MATERIALS.put("WORLD", Material.GRASS_BLOCK);
        CATEGORY_MATERIALS.put("BLOCK", Material.COBBLESTONE);
        CATEGORY_MATERIALS.put("ENTITY", Material.ZOMBIE_SPAWN_EGG);
        CATEGORY_MATERIALS.put("REDSTONE", Material.REDSTONE);
        CATEGORY_MATERIALS.put("MISC", Material.NETHER_STAR);
    }
    
    /**
     * Инициализирует графический интерфейс игровых событий
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     * @param blockMaterial Материал блока для настройки
     */
    public GameEventBlockGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
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
        this.inventory = Bukkit.createInventory(null, 54, "§8Игровое событие: " + getBlockDisplayName());
        
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
        infoLore.add("§7Выберите категорию игровых событий");
        infoLore.add("");
        infoLore.add("§aКликните по категории чтобы");
        infoLore.add("§aпросмотреть доступные события");
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
        return "Game Event Block GUI for " + blockMaterial.name();
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
            // Find event ID in lore
            String eventId = null;
            for (String line : lore) {
                if (line.startsWith("§8ID: ")) {
                    eventId = line.substring(5).trim(); // Remove "§8ID: " prefix
                    break;
                }
            }
            
            if (eventId != null && !eventId.isEmpty()) {
                selectEvent(eventId);
            } else {
                player.sendMessage("§eℹ Это заголовок категории. Кликните по событию ниже.");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            }
        }
    }
    
    /**
     * Открывает графический интерфейс выбора событий в категории
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
        
        // Load events for this category
        loadEventsForCategory(categoryInventory, category);
        
        // Open the category inventory
        player.openInventory(categoryInventory);
    }
    
    /**
     * Загружает события для категории
     * @param inventory Инвентарь для заполнения
     * @param category Категория для загрузки
     */
    private void loadEventsForCategory(Inventory inventory, String category) {
        // Check if blockConfigService is available
        if (blockConfigService == null) {
            player.sendMessage("§cОшибка: Сервис конфигурации блоков недоступен!");
            return;
        }
        
        // Get available events for this block material
        List<String> availableEvents = blockConfigService.getActionsForMaterial(blockMaterial);
        
        // Filter events by category
        List<String> categoryEvents = new ArrayList<>();
        for (String eventId : availableEvents) {
            String eventCategory = getGameEventCategory(eventId);
            if (category.equals(eventCategory)) {
                categoryEvents.add(eventId);
            }
        }
        
        // Create event items
        int slot = 10;
        for (String eventId : categoryEvents) {
            if (slot >= 44) break; // Don't go into border area
            
            ItemStack eventItem = createEventItem(eventId);
            inventory.setItem(slot, eventItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2; // Skip border slots
        }
    }
    
    /**
     * Создает элемент события
     * @param eventId ID события
     * @return ItemStack элемент события
     */
    private ItemStack createEventItem(String eventId) {
        // Create appropriate material for event type
        Material material = getEventMaterial(eventId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§a§l" + getEventDisplayName(eventId));
        
        // Set lore with description and category
        List<String> lore = new ArrayList<>();
        lore.add("§7" + getEventDescription(eventId));
        lore.add("");
        lore.add("§8⚙️ Категория: " + getEventCategoryName(eventId));
        lore.add("");
        lore.add("§e⚡ Кликните чтобы выбрать");
        lore.add("§8ID: " + eventId);
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Получает отображаемое имя категории для события
     * @param eventId ID события
     * @return Отображаемое имя категории
     */
    private String getEventCategoryName(String eventId) {
        String category = getGameEventCategory(eventId);
        return CATEGORY_NAMES.getOrDefault(category, category);
    }
    
    /**
     * Получает категорию для игрового события
     * @param eventId ID события
     * @return Категория события
     */
    private String getGameEventCategory(String eventId) {
        switch (eventId.toLowerCase()) {
            case "onserverstart":
            case "onserverstop":
                return "SERVER";
            case "ontimechange":
            case "onweatherchange":
            case "isnight":
                return "WORLD";
            case "onblockburn":
            case "onblockfade":
            case "onblockform":
                return "BLOCK";
            case "onentityspawn":
            case "onentitydeath":
            case "onentitydamage":
                return "ENTITY";
            case "onredstonesignal":
            case "onpistonmove":
                return "REDSTONE";
            default:
                return "MISC";
        }
    }
    
    /**
     * Получает материал для события
     * @param eventId ID события
     * @return Материал для события
     */
    private Material getEventMaterial(String eventId) {
        // Return appropriate materials based on event type
        switch (eventId.toLowerCase()) {
            case "onserverstart":
            case "onserverstop":
                return Material.COMMAND_BLOCK;
            case "ontimechange":
                return Material.CLOCK;
            case "onweatherchange":
                return Material.WATER_BUCKET;
            case "onblockburn":
                return Material.FLINT_AND_STEEL;
            case "onblockfade":
                return Material.DEAD_BUSH;
            case "onentityspawn":
                return Material.EGG;
            case "onentitydeath":
                return Material.SKELETON_SKULL;
            case "onentitydamage":
                return Material.RED_DYE;
            case "onredstonesignal":
                return Material.REDSTONE;
            case "onpistonmove":
                return Material.PISTON;
            default:
                return Material.STONE;
        }
    }
    
    /**
     * Получает отображаемое имя события
     * @param eventId ID события
     * @return Отображаемое имя события
     */
    private String getEventDisplayName(String eventId) {
        // Return user-friendly names for events
        switch (eventId.toLowerCase()) {
            case "onserverstart": return "Запуск сервера";
            case "onserverstop": return "Остановка сервера";
            case "ontimechange": return "Смена времени";
            case "onweatherchange": return "Смена погоды";
            case "onblockburn": return "Горение блока";
            case "onblockfade": return "Исчезновение блока";
            case "onblockform": return "Формирование блока";
            case "onentityspawn": return "Появление сущности";
            case "onentitydeath": return "Смерть сущности";
            case "onentitydamage": return "Урон сущности";
            case "onredstonesignal": return "Сигнал редстоуна";
            case "onpistonmove": return "Движение поршня";
            default: return eventId;
        }
    }

    /**
     * Получает описание события
     * @param eventId ID события
     * @return Описание события
     */
    private String getEventDescription(String eventId) {
        // Return descriptions for events
        switch (eventId.toLowerCase()) {
            case "onserverstart": return "Срабатывает при запуске сервера";
            case "onserverstop": return "Срабатывает при остановке сервера";
            case "ontimechange": return "Срабатывает при смене времени суток";
            case "onweatherchange": return "Срабатывает при смене погоды";
            case "onblockburn": return "Срабатывает когда блок начинает гореть";
            case "onblockfade": return "Срабатывает когда блок исчезает (например, лед тает)";
            case "onblockform": return "Срабатывает когда блок формируется (например, лед образуется)";
            case "onentityspawn": return "Срабатывает когда сущность появляется в мире";
            case "onentitydeath": return "Срабатывает когда сущность умирает";
            case "onentitydamage": return "Срабатывает когда сущность получает урон";
            case "onredstonesignal": return "Срабатывает при получении сигнала редстоуна";
            case "onpistonmove": return "Срабатывает при движении поршня";
            default: return "Игровое событие " + eventId;
        }
    }
    
    /**
     * Выбирает событие для блока
     * @param eventId ID события
     */
    private void selectEvent(String eventId) {
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
        
        // Set the event
        codeBlock.setAction(eventId);
        
        // Save the world
        var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
        }
        
        // Notify player
        player.sendMessage("§a✓ Событие '" + getEventDisplayName(eventId) + "' установлено!");
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