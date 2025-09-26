package com.megacreative.gui.coding.entity_action;

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
 * 🎆 УЛУЧШЕННЫЙ Графический интерфейс действий над сущностями
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 ENHANCED Entity Action Block GUI
 * 
 * Implements reference system-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 ERWEITERT Entitätsaktions-Block-GUI
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
 */
public class EntityActionBlockGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    // Categories for different types of entity actions
    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();
    private static final Map<String, Material> CATEGORY_MATERIALS = new HashMap<>();
    
    static {
        // Define category names and their display names
        CATEGORY_NAMES.put("SPAWN", "🧟 Создание");
        CATEGORY_NAMES.put("DAMAGE", "⚔️ Урон");
        CATEGORY_NAMES.put("MOVEMENT", "🏃 Перемещение");
        CATEGORY_NAMES.put("EFFECTS", "🧪 Эффекты");
        CATEGORY_NAMES.put("AI", "🤖 ИИ");
        CATEGORY_NAMES.put("MISC", "🔧 Другое");
        
        // Define materials for category items
        CATEGORY_MATERIALS.put("SPAWN", Material.ZOMBIE_SPAWN_EGG);
        CATEGORY_MATERIALS.put("DAMAGE", Material.DIAMOND_SWORD);
        CATEGORY_MATERIALS.put("MOVEMENT", Material.LEATHER_BOOTS);
        CATEGORY_MATERIALS.put("EFFECTS", Material.POTION);
        CATEGORY_MATERIALS.put("AI", Material.REDSTONE);
        CATEGORY_MATERIALS.put("MISC", Material.CHEST);
    }
    
    /**
     * Инициализирует графический интерфейс действий над сущностями
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     * @param blockMaterial Материал блока для настройки
     */
    public EntityActionBlockGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
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
        this.inventory = Bukkit.createInventory(null, 54, "§8Действие над сущностью: " + getBlockDisplayName());
        
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
        infoLore.add("§7Выберите категорию действий над сущностями");
        infoLore.add("");
        infoLore.add("§aКликните по категории чтобы");
        infoLore.add("§aпросмотреть доступные действия");
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
        return "Entity Action Block GUI for " + blockMaterial.name();
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
            // Find action ID in lore
            String actionId = null;
            for (String line : lore) {
                if (line.startsWith("§8ID: ")) {
                    actionId = line.substring(5).trim(); // Remove "§8ID: " prefix
                    break;
                }
            }
            
            if (actionId != null && !actionId.isEmpty()) {
                selectAction(actionId);
            } else {
                player.sendMessage("§eℹ Это заголовок категории. Кликните по действию ниже.");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            }
        }
    }
    
    /**
     * Открывает графический интерфейс выбора действий в категории
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
        
        // Load actions for this category
        loadActionsForCategory(categoryInventory, category);
        
        // Open the category inventory
        player.openInventory(categoryInventory);
    }
    
    /**
     * Загружает действия для категории
     * @param inventory Инвентарь для заполнения
     * @param category Категория для загрузки
     */
    private void loadActionsForCategory(Inventory inventory, String category) {
        // Check if blockConfigService is available
        if (blockConfigService == null) {
            player.sendMessage("§cОшибка: Сервис конфигурации блоков недоступен!");
            return;
        }
        
        // Get available actions for this block material
        List<String> availableActions = blockConfigService.getActionsForMaterial(blockMaterial);
        
        // Filter actions by category
        List<String> categoryActions = new ArrayList<>();
        for (String actionId : availableActions) {
            String actionCategory = getEntityActionCategory(actionId);
            if (category.equals(actionCategory)) {
                categoryActions.add(actionId);
            }
        }
        
        // Create action items
        int slot = 10;
        for (String actionId : categoryActions) {
            if (slot >= 44) break; // Don't go into border area
            
            ItemStack actionItem = createActionItem(actionId);
            inventory.setItem(slot, actionItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2; // Skip border slots
        }
    }
    
    /**
     * Создает элемент действия
     * @param actionId ID действия
     * @return ItemStack элемент действия
     */
    private ItemStack createActionItem(String actionId) {
        // Create appropriate material for action type
        Material material = getActionMaterial(actionId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§a§l" + getActionDisplayName(actionId));
        
        // Set lore with description and category
        List<String> lore = new ArrayList<>();
        lore.add("§7" + getActionDescription(actionId));
        lore.add("");
        lore.add("§8⚙️ Категория: " + getActionCategoryName(actionId));
        lore.add("");
        lore.add("§e⚡ Кликните чтобы выбрать");
        lore.add("§8ID: " + actionId);
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Получает отображаемое имя категории для действия
     * @param actionId ID действия
     * @return Отображаемое имя категории
     */
    private String getActionCategoryName(String actionId) {
        String category = getEntityActionCategory(actionId);
        return CATEGORY_NAMES.getOrDefault(category, category);
    }
    
    /**
     * Получает категорию для действия над сущностью
     * @param actionId ID действия
     * @return Категория действия
     */
    private String getEntityActionCategory(String actionId) {
        switch (actionId.toLowerCase()) {
            case "spawnentity":
            case "spawnmob":
                return "SPAWN";
            case "damageentity":
            case "killentity":
                return "DAMAGE";
            case "teleportentity":
            case "setentityvelocity":
                return "MOVEMENT";
            case "giveentitypotioneffect":
                return "EFFECTS";
            case "setentityai":
                return "AI";
            default:
                return "MISC";
        }
    }
    
    /**
     * Получает материал для действия
     * @param actionId ID действия
     * @return Материал для действия
     */
    private Material getActionMaterial(String actionId) {
        // Return appropriate materials based on action type
        switch (actionId.toLowerCase()) {
            case "spawnentity":
                return Material.ZOMBIE_SPAWN_EGG;
            case "spawnmob":
                return Material.SKELETON_SPAWN_EGG;
            case "damageentity":
                return Material.DIAMOND_SWORD;
            case "killentity":
                return Material.BARRIER;
            case "teleportentity":
                return Material.ENDER_PEARL;
            case "setentityvelocity":
                return Material.FEATHER;
            case "setentityai":
                return Material.REDSTONE;
            case "giveentitypotioneffect":
                return Material.POTION;
            default:
                return Material.STONE;
        }
    }
    
    /**
     * Получает отображаемое имя действия
     * @param actionId ID действия
     * @return Отображаемое имя действия
     */
    private String getActionDisplayName(String actionId) {
        // Return user-friendly names for actions
        switch (actionId.toLowerCase()) {
            case "spawnentity": return "Создать существо";
            case "spawnmob": return "Создать моба";
            case "damageentity": return "Нанести урон";
            case "killentity": return "Убить существо";
            case "teleportentity": return "Телепортировать";
            case "setentityvelocity": return "Установить скорость";
            case "setentityai": return "Установить ИИ";
            case "giveentitypotioneffect": return "Дать эффект";
            default: return actionId;
        }
    }

    /**
     * Получает описание действия
     * @param actionId ID действия
     * @return Описание действия
     */
    private String getActionDescription(String actionId) {
        // Return descriptions for actions
        switch (actionId.toLowerCase()) {
            case "spawnentity": return "Создает существо в мире";
            case "spawnmob": return "Создает моба в мире";
            case "damageentity": return "Наносит урон существу";
            case "killentity": return "Убивает существо";
            case "teleportentity": return "Телепортирует существо";
            case "setentityvelocity": return "Устанавливает скорость движения существа";
            case "setentityai": return "Включает или выключает ИИ существа";
            case "giveentitypotioneffect": return "Накладывает эффект зелья на существо";
            default: return "Действие " + actionId;
        }
    }
    
    /**
     * Выбирает действие для блока
     * @param actionId ID действия
     */
    private void selectAction(String actionId) {
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
        
        // Set the action
        codeBlock.setAction(actionId);
        
        // Save the world
        var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
        }
        
        // Notify player
        player.sendMessage("§a✓ Действие '" + getActionDisplayName(actionId) + "' установлено!");
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