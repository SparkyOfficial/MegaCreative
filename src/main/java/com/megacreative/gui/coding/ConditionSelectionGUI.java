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
 * 🎆 УЛУЧШЕННЫЙ Графический интерфейс для выбора условий для блоков кода.
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 ENHANCED GUI for selecting conditions for code blocks.
 * 
 * Implements reference system-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 ERWEITERT GUI zur Auswahl von Bedingungen für Codeblöcke.
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
 */
public class ConditionSelectionGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    
    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();
    private static final Map<String, Material> CATEGORY_MATERIALS = new HashMap<>();
    
    static {
        
        CATEGORY_NAMES.put("PLAYER", "👤 Игрок");
        CATEGORY_NAMES.put("WORLD", "🌍 Мир");
        CATEGORY_NAMES.put("ITEM", "🎁 Предметы");
        CATEGORY_NAMES.put("VARIABLE", "📊 Переменные");
        CATEGORY_NAMES.put("ENTITY", "🧟 Существа");
        CATEGORY_NAMES.put("GAME", "🎮 Игра");
        
        
        CATEGORY_MATERIALS.put("PLAYER", Material.PLAYER_HEAD);
        CATEGORY_MATERIALS.put("WORLD", Material.GRASS_BLOCK);
        CATEGORY_MATERIALS.put("ITEM", Material.CHEST);
        CATEGORY_MATERIALS.put("VARIABLE", Material.NAME_TAG);
        CATEGORY_MATERIALS.put("ENTITY", Material.ZOMBIE_SPAWN_EGG);
        CATEGORY_MATERIALS.put("GAME", Material.COMMAND_BLOCK);
    }
    
    
    /**
     * Инициализирует графический интерфейс выбора условий
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     * @param blockMaterial Материал блока для настройки
     */
    public ConditionSelectionGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        
        // Initialize block config service directly since plugin is never null
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        this.inventory = Bukkit.createInventory(null, 54, "§8Выбор условия: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    /**
     * Получает отображаемое имя блока
     */
    private String getBlockDisplayName() {
        
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        return config != null ? config.getDisplayName() : blockMaterial.name();
    }
    
    /**
     * Настраивает инвентарь графического интерфейса
     */
    private void setupInventory() {
        inventory.clear();
        
        
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderItem);
            }
        }
        
        
        ItemStack infoItem = new ItemStack(blockMaterial);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e§l" + getBlockDisplayName());
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Выберите категорию условий");
        infoLore.add("");
        infoLore.add("§aКликните по категории чтобы");
        infoLore.add("§aпросмотреть доступные условия");
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        
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
            
            slot += 2; 
            if (slot >= 44) break; 
        }
    }
    
    /**
     * Открывает графический интерфейс для игрока
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        
        
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
            player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 1);
    }
    
    /**
     * Получает заголовок графического интерфейса
     * @return Заголовок интерфейса
     */
    @Override
    public String getGUITitle() {
        return "Condition Selection GUI for " + blockMaterial.name();
    }
    
    /**
     * Обрабатывает события кликов в инвентаре
     * @param event Событие клика в инвентаре
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true); 
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        ItemMeta meta = clicked.getItemMeta();
        String displayName = meta.getDisplayName();
        
        
        for (Map.Entry<String, String> category : CATEGORY_NAMES.entrySet()) {
            String categoryName = category.getValue();
            if (displayName.contains(categoryName)) {
                
                openCategorySelectionGUI(category.getKey());
                return;
            }
        }
        
        
        List<String> lore = meta.getLore();
        if (lore != null) {
            
            String conditionId = null;
            for (String line : lore) {
                if (line.startsWith("§8ID: ")) {
                    conditionId = line.substring(5).trim(); 
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
        
        Inventory categoryInventory = Bukkit.createInventory(null, 54, "§8" + CATEGORY_NAMES.getOrDefault(category, category));
        
        
        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                categoryInventory.setItem(i, borderItem);
            }
        }
        
        
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c⬅ Назад");
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Вернуться к выбору категорий");
        backMeta.setLore(backLore);
        backButton.setItemMeta(backMeta);
        categoryInventory.setItem(49, backButton);
        
        
        loadConditionsForCategory(categoryInventory, category);
        
        
        player.openInventory(categoryInventory);
    }
    
    /**
     * Загружает условия для категории
     * @param inventory Инвентарь для заполнения
     * @param category Категория для загрузки
     */
    private void loadConditionsForCategory(Inventory inventory, String category) {
        
        if (blockConfigService == null) {
            player.sendMessage("§cОшибка: Сервис конфигурации блоков недоступен!");
            return;
        }
        
        
        List<String> availableConditions = blockConfigService.getActionsForMaterial(blockMaterial);
        
        
        List<String> categoryConditions = new ArrayList<>();
        for (String conditionId : availableConditions) {
            String conditionCategory = getConditionCategory(conditionId);
            if (category.equals(conditionCategory)) {
                categoryConditions.add(conditionId);
            }
        }
        
        
        int slot = 10;
        for (String conditionId : categoryConditions) {
            if (slot >= 44) break; 
            
            ItemStack conditionItem = createConditionItem(conditionId);
            inventory.setItem(slot, conditionItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2; 
        }
    }
    
    /**
     * Создает элемент условия
     * @param conditionId ID условия
     * @return ItemStack элемент условия
     */
    private ItemStack createConditionItem(String conditionId) {
        
        Material material = getConditionMaterial(conditionId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        
        meta.setDisplayName("§a§l" + getConditionDisplayName(conditionId));
        
        
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
        String category = getConditionCategory(conditionId);
        return CATEGORY_NAMES.getOrDefault(category, category);
    }
    
    /**
     * Получает категорию для условия
     * @param conditionId ID условия
     * @return Категория условия
     */
    private String getConditionCategory(String conditionId) {
        switch (conditionId.toLowerCase()) {
            case "hasitem":
            case "removeitems":
            case "isplayerholding":
            case "hasarmor":
            case "checkplayerinventory":
                return "ITEM";
            case "isop":
            case "playergamemode":
            case "playerhealth":
            case "isriding":
            case "checkplayerstats":
                return "PLAYER";
            case "isinworld":
            case "worldtime":
            case "isnight":
            case "checkworldweather":
            case "isblocktype":
            case "isnearblock":
                return "WORLD";
            case "ifvarequals":
            case "ifvargreater":
            case "ifvarless":
            case "comparevariable":
            case "setvar":
            case "getvar":
            case "addvar":
            case "subvar":
            case "mulvar":
            case "divvar":
                return "VARIABLE";
            case "mobnear":
                return "ENTITY";
            case "checkserveronline":
                return "GAME";
            default:
                return "PLAYER";
        }
    }
    
    /**
     * Получает материал для условия
     * @param conditionId ID условия
     * @return Материал для условия
     */
    private Material getConditionMaterial(String conditionId) {
        
        switch (conditionId.toLowerCase()) {
            case "hasitem":
            case "checkplayerinventory":
                return Material.CHEST;
            case "isop":
                return Material.COMMAND_BLOCK;
            case "playergamemode":
                return Material.GRASS_BLOCK;
            case "playerhealth":
                return Material.GOLDEN_APPLE;
            case "isnight":
                return Material.BLACK_WOOL;
            case "isriding":
                return Material.SADDLE;
            case "isinworld":
                return Material.NETHER_STAR;
            case "worldtime":
                return Material.CLOCK;
            case "isnearblock":
                return Material.STONE;
            case "mobnear":
                return Material.ZOMBIE_SPAWN_EGG;
            case "ifvarequals":
            case "ifvargreater":
            case "ifvarless":
            case "comparevariable":
                return Material.COMPARATOR;
            case "checkplayerstats":
                return Material.OAK_SIGN;
            case "checkserveronline":
                return Material.REDSTONE_LAMP;
            case "checkworldweather":
                return Material.SNOWBALL;
            case "worldguardregioncheck":
                return Material.BRICKS;
            case "isblocktype":
                return Material.COBBLESTONE;
            case "isplayerholding":
                return Material.WOODEN_SWORD;
            case "hasarmor":
                return Material.DIAMOND_CHESTPLATE;
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
        
        switch (conditionId.toLowerCase()) {
            case "hasitem": return "Если есть предмет";
            case "isop": return "Если оператор";
            case "playergamemode": return "Если режим игры";
            case "playerhealth": return "Если здоровье";
            case "isnight": return "Если ночь";
            case "isriding": return "Если едет";
            case "isinworld": return "Если в мире";
            case "worldtime": return "Если время мира";
            case "isnearblock": return "Если рядом блок";
            case "mobnear": return "Если рядом моб";
            case "ifvarequals": return "Если переменная равна";
            case "ifvargreater": return "Если переменная больше";
            case "ifvarless": return "Если переменная меньше";
            case "comparevariable": return "Сравнить переменные";
            case "checkplayerstats": return "Проверить статистику";
            case "checkserveronline": return "Проверить сервер";
            case "checkworldweather": return "Проверить погоду";
            case "worldguardregioncheck": return "Проверить регион";
            case "isblocktype": return "Если тип блока";
            case "isplayerholding": return "Если держит";
            case "hasarmor": return "Если есть броня";
            case "setvar": return "Установить переменную";
            case "getvar": return "Получить переменную";
            case "addvar": return "Добавить к переменной";
            case "subvar": return "Вычесть из переменной";
            default: return conditionId;
        }
    }

    /**
     * Получает описание условия
     * @param conditionId ID условия
     * @return Описание условия
     */
    private String getConditionDescription(String conditionId) {
        
        switch (conditionId.toLowerCase()) {
            case "hasitem": return "Проверяет есть ли предмет у игрока";
            case "isop": return "Проверяет является ли игрок оператором";
            case "playergamemode": return "Проверяет режим игры игрока";
            case "playerhealth": return "Проверяет здоровье игрока";
            case "isnight": return "Проверяет ночь ли сейчас";
            case "isriding": return "Проверяет едет ли игрок";
            case "isinworld": return "Проверяет находится ли игрок в мире";
            case "worldtime": return "Проверяет время в мире";
            case "isnearblock": return "Проверяет находится ли рядом блок";
            case "mobnear": return "Проверяет находится ли рядом моб";
            case "ifvarequals": return "Проверяет равенство переменной";
            case "ifvargreater": return "Проверяет больше ли переменная";
            case "ifvarless": return "Проверяет меньше ли переменная";
            case "comparevariable": return "Сравнивает две переменные";
            case "checkplayerstats": return "Проверяет статистику игрока";
            case "checkserveronline": return "Проверяет статус сервера";
            case "checkworldweather": return "Проверяет погоду в мире";
            case "worldguardregioncheck": return "Проверяет находится ли игрок в регионе";
            case "isblocktype": return "Проверяет тип блока";
            case "isplayerholding": return "Проверяет что держит игрок";
            case "hasarmor": return "Проверяет есть ли броня у игрока";
            case "setvar": return "Создает/изменяет переменную";
            case "getvar": return "Получает значение переменной";
            case "addvar": return "Добавляет к переменной";
            case "subvar": return "Вычитает из переменной";
            default: return "Условие " + conditionId;
        }
    }
    
    /**
     * Выбирает условие для блока
     * @param conditionId ID условия
     */
    private void selectCondition(String conditionId) {
        
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
        
        
        codeBlock.setAction(conditionId);
        
        
        var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
        }
        
        
        player.sendMessage("§a✓ Условие '" + getConditionDisplayName(conditionId) + "' установлено!");
        player.sendMessage("§eКликните снова по блоку для настройки параметров.");
        
        
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, 
            player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 1);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        
        
        player.closeInventory();
    }
}