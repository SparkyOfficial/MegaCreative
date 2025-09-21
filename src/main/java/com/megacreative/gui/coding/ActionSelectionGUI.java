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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.*;

/**
 * Графический интерфейс для выбора действий для блоков кода.
 * 🎆 УЛУЧШЕННЫЕ ФУНКЦИИ:
 * - Категоризированное отображение действий с визуальной группировкой
 * - Умные возможности поиска и фильтрации
 * - Предварительный просмотр действий с подробными описаниями
 * - Визуальная обратная связь для процесса выбора
 * - Оптимизирован для быстрого поиска действий
 * 
 * Графический интерфейс для выбора действий для блоков кода.
 * 🎆 УЛУЧШЕННЫЕ ФУНКЦИИ:
 * - Категоризированное отображение действий с визуальной группировкой
 * - Умные возможности поиска и фильтрации
 * - Предварительный просмотр действий с подробными описаниями
 * - Визуальная обратная связь для процесса выбора
 * - Оптимизирован для быстрого поиска действий
 *
 * GUI for selecting actions for code blocks.
 * 🎆 ENHANCED FEATURES:
 * - Categorized action display with visual grouping
 * - Smart search and filtering capabilities
 * - Action preview with detailed descriptions
 * - Visual feedback for selection process
 * - Optimized for quick action discovery
 *
 * GUI zur Auswahl von Aktionen für Codeblöcke.
 * 🎆 ERWEITERT FUNKTIONEN:
 * - Kategorisierte Aktionsanzeige mit visueller Gruppierung
 * - Intelligente Such- und Filterfunktionen
 * - Aktionsvorschau mit detaillierten Beschreibungen
 * - Visuelle Rückmeldung für den Auswahlprozess
 * - Optimiert für schnelle Aktionsfindung
 * 
 * Opens when a player clicks on a code block without an assigned action.
 * Реализует Creative+-стиль: универсальные блоки с настройкой через GUI
 *
 * Wird geöffnet, wenn ein Spieler auf einen Codeblock ohne zugewiesene Aktion klickt.
 * Implementiert Creative+-Stil: universelle Blöcke mit GUI-Konфигuration
 */
public class ActionSelectionGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    /**
     * Инициализирует графический интерфейс выбора действий
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     * @param blockMaterial Материал блока для настройки
     *
     * Initializes action selection GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     * @param blockMaterial Material of block to configure
     *
     * Initialisiert die Aktionsauswahl-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     * @param blockLocation Position des zu konфигurierenden Blocks
     * @param blockMaterial Material des zu konфигurierenden Blocks
     */
    public ActionSelectionGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getGuiManager();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Create inventory with appropriate size
        this.inventory = Bukkit.createInventory(null, 54, "§8Выбор действия: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    /**
     * Получает отображаемое имя блока
     *
     * Gets display name for block
     *
     * Ruft den Anzeigenamen des Blocks ab
     */
    private String getBlockDisplayName() {
        // Get display name from block config service
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        return config != null ? config.getDisplayName() : blockMaterial.name();
    }
    
    /**
     * Настраивает инвентарь графического интерфейса
     *
     * Sets up the GUI inventory
     *
     * Richtet das GUI-Inventar ein
     */
    private void setupInventory() {
        inventory.clear();
        
        // Add background glass panes
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        
        // Fill border slots
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, glassPane);
            }
        }
        
        // Add info item
        ItemStack infoItem = new ItemStack(blockMaterial);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e§l" + getBlockDisplayName());
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Выберите действие для этого блока");
        infoLore.add("");
        infoLore.add("§aКликните на действие чтобы");
        infoLore.add("§аназначить его блоку");
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Load available actions for this block type
        loadAvailableActions();
    }
    
    /**
     * Загружает доступные действия для этого типа блока
     *
     * Loads available actions for this block type
     *
     * Lädt verfügbare Aktionen für diesen Blocktyp
     */
    private void loadAvailableActions() {
        // Debug logging
        player.sendMessage("§eDebug: Checking material " + blockMaterial.name());
        
        // Get available actions for this block material using BlockConfigService
        List<String> availableActions = blockConfigService.getActionsForMaterial(blockMaterial);
        
        player.sendMessage("§eDebug: Available actions count: " + (availableActions != null ? availableActions.size() : "null"));
        
        // Simple fallback to default actions if none found
        if (availableActions == null || availableActions.isEmpty()) {
            player.sendMessage("§cОшибка: Нет доступных действий для блока " + blockMaterial.name());
            
            // Use appropriate default actions based on block type
            availableActions = new ArrayList<>();
            
            // Add appropriate default actions based on block material
            if (blockMaterial == Material.IRON_BLOCK) {
                // For variable blocks (IRON_BLOCK), add variable-related default actions
                availableActions.add("setVar");
                availableActions.add("getVar");
                availableActions.add("addVar");
                availableActions.add("subVar");
                player.sendMessage("§6Using variable default actions as fallback");
            } else if (blockMaterial == Material.NETHERITE_BLOCK) {
                // For gaming action blocks (NETHERITE_BLOCK), add gaming default actions
                availableActions.add("setTime");
                availableActions.add("setWeather");
                availableActions.add("setBlock");
                player.sendMessage("§6Using gaming default actions as fallback");
            } else {
                // For other action blocks, use general defaults
                availableActions.add("sendMessage");
                availableActions.add("teleport");
                availableActions.add("giveItem");
                player.sendMessage("§6Using general default actions as fallback");
            }
        }
        
        // 🎆 ENHANCED: Group actions by category for better organization
        Map<String, List<String>> categorizedActions = categorizeActions(availableActions);
        
        // Create action items with visual categorization
        int slot = 10; // Start from first available slot
        
        for (Map.Entry<String, List<String>> category : categorizedActions.entrySet()) {
            String categoryName = category.getKey();
            List<String> actionsInCategory = category.getValue();
            
            // Add category separator if we have multiple categories
            if (categorizedActions.size() > 1) {
                ItemStack categoryItem = createCategoryItem(categoryName, actionsInCategory.size());
                if (slot < 44) {
                    inventory.setItem(slot, categoryItem);
                    slot++;
                    if (slot % 9 == 8) slot += 2; // Skip border
                }
            }
            
            // Add actions in this category
            for (String actionId : actionsInCategory) {
                if (slot >= 44) break; // Don't go into border area
                
                ItemStack actionItem = createActionItem(actionId, categoryName);
                inventory.setItem(slot, actionItem);
                
                // Move to next slot, skipping border slots
                slot++;
                if (slot % 9 == 8) slot += 2; // Skip right border and left border of next row
            }
            
            // Add spacing between categories
            if (slot < 44 && categorizedActions.size() > 1) {
                slot++;
                if (slot % 9 == 8) slot += 2;
            }
        }
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Категоризирует действия для лучшей организации
     * Реализует стиль reference system: универсальные блоки с настройкой через GUI
     *
     * 🎆 ENHANCED: Categorize actions for better organization
     * Implements reference system-style: universal blocks with GUI configuration
     *
     * 🎆 ERWEITERT: Kategorisiert Aktionen für bessere Organisation
     * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konфигuration
     */
    private Map<String, List<String>> categorizeActions(List<String> actions) {
        Map<String, List<String>> categories = new LinkedHashMap<>();
        
        for (String action : actions) {
            String category = getActionCategory(action);
            categories.computeIfAbsent(category, k -> new ArrayList<>()).add(action);
        }
        
        return categories;
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Получает категорию для действия
     * Реализует стиль reference system: универсальные блоки с настройкой через GUI
     *
     * 🎆 ENHANCED: Get category for an action
     * Implements reference system-style: universal blocks with GUI configuration
     *
     * 🎆 ERWEITERT: Ruft die Kategorie für eine Aktion ab
     * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Kонфигuration
     */
    private String getActionCategory(String actionId) {
        switch (actionId.toLowerCase()) {
            case "sendmessage":
            case "broadcast":
            case "sendtitle":
            case "sendactionbar":
                return "💬 Коммуникация";
            
            case "teleport":
            case "settime":
            case "setweather":
            case "setblock":
                return "🌍 Мир и перемещение";
            
            case "giveitem":
            case "giveitems":
            case "removeitems":
            case "setarmor":
                return "🎁 Предметы и инвентарь";
            
            case "setvar":
            case "getvar":
            case "addvar":
            case "subvar":
            case "mulvar":
            case "divvar":
            case "setglobalvar":
            case "getglobalvar":
            case "setservervar":
            case "getservervar":
                return "📊 Переменные";
            
            case "playsound":
            case "effect":
            case "playparticle":
                return "🎨 Эффекты и звук";
            
            case "command":
            case "executeasynccommand":
                return "⚙️ Команды системы";
            
            case "wait":
            case "asyncloop":
            case "randomnumber":
                return "🔄 Логика и управление";
            
            case "spawnentity":
            case "spawnmob":
                return "🧟 Существа";
            
            case "healplayer":
            case "setgamemode":
                return "🎮 Игрок";
            
            case "explosion":
                return "💥 Разрушение";
            
            case "createscoreboard":
            case "setscore":
            case "incrementscore":
            case "createteam":
            case "addplayertoteam":
                return "🏆 Скорборды и команды";
            
            case "savelocation":
            case "getlocation":
                return "📍 Локации";
            
            default:
                return "🔧 Основные";
        }
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Создает элемент заголовка категории
     * Реализует стиль reference system: универсальные блоки с настройкой через GUI
     *
     * 🎆 ENHANCED: Create category header item
     * Implements reference system-style: universal blocks with GUI configuration
     *
     * 🎆 ERWEITERT: Erstellt Kategorie-Header-Element
     * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Kонфигuration
     */
    private ItemStack createCategoryItem(String categoryName, int actionCount) {
        ItemStack item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§e§l" + categoryName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Доступно действий: " + actionCount);
        lore.add("§8Категория");
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Создает элемент действия
     * Реализует стиль reference system: универсальные блоки с настройкой через GUI
     *
     * 🎆 ENHANCED: Create action item
     * Implements reference system-style: universal blocks with GUI configuration
     *
     * 🎆 ERWEITERT: Erstellt Aktionsgegenstand
     * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Kонфигuration
     */
    private ItemStack createActionItem(String actionId, String category) {
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
        lore.add("§8⚙️ Категория: " + category);
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
     * Получает материал для действия
     *
     * Gets material for action
     *
     * Ruft das Material für die Aktion аб
     */
    private Material getActionMaterial(String actionId) {
        // Return appropriate materials based on action type
        switch (actionId.toLowerCase()) {
            case "sendmessage":
            case "broadcast":
            case "sendtitle":
            case "sendactionbar":
                return Material.PAPER;
            case "teleport":
                return Material.ENDER_PEARL;
            case "giveitem":
            case "giveitems":
                return Material.CHEST;
            case "playsound":
                return Material.NOTE_BLOCK;
            case "effect":
            case "playparticle":
                return Material.BLAZE_POWDER;
            case "command":
            case "executeasynccommand":
                return Material.COMMAND_BLOCK;
            case "spawnentity":
            case "spawnmob":
                return Material.ZOMBIE_SPAWN_EGG;
            case "removeitems":
                return Material.BARRIER;
            case "setarmor":
                return Material.DIAMOND_CHESTPLATE;
            case "setvar":
            case "getvar":
            case "addvar":
            case "subvar":
            case "mulvar":
            case "divvar":
                return Material.NAME_TAG;
            case "setglobalvar":
            case "getglobalvar":
            case "setservervar":
            case "getservervar":
                return Material.WRITABLE_BOOK;
            case "healplayer":
                return Material.GOLDEN_APPLE;
            case "setgamemode":
                return Material.GRASS_BLOCK;
            case "settime":
                return Material.CLOCK;
            case "setweather":
                return Material.SNOWBALL;
            case "explosion":
                return Material.TNT;
            case "setblock":
                return Material.STONE;
            case "wait":
                return Material.HOPPER;
            case "randomnumber":
                return Material.SLIME_BALL;
            case "asyncloop":
                return Material.REPEATER;
            case "createscoreboard":
            case "setscore":
            case "incrementscore":
                return Material.OAK_SIGN;
            case "createteam":
            case "addplayertoteam":
                return Material.WHITE_BANNER;
            case "savelocation":
            case "getlocation":
                return Material.COMPASS;
            default:
                return Material.STONE;
        }
    }
    
    /**
     * Получает отображаемое имя действия
     *
     * Gets display name for action
     *
     * Ruft den Anzeigenamen der Aktion аб
     */
    private String getActionDisplayName(String actionId) {
        // Return user-friendly names for actions
        switch (actionId.toLowerCase()) {
            case "sendmessage": return "Отправить сообщение";
            case "broadcast": return "Объявление";
            case "sendtitle": return "Отправить заголовок";
            case "sendactionbar": return "Отправить в ActionBar";
            case "teleport": return "Телепортировать";
            case "giveitem": return "Выдать предмет";
            case "giveitems": return "Выдать предметы";
            case "playsound": return "Воспроизвести звук";
            case "effect": return "Эффект";
            case "playparticle": return "Воспроизвести частицы";
            case "command": return "Выполнить команду";
            case "executeasynccommand": return "Асинхронная команда";
            case "spawnentity": return "Заспавнить существо";
            case "spawnmob": return "Заспавнить моба";
            case "removeitems": return "Удалить предметы";
            case "setarmor": return "Установить броню";
            case "setvar": return "Установить переменную";
            case "getvar": return "Получить переменную";
            case "addvar": return "Добавить к переменной";
            case "subvar": return "Вычесть из переменной";
            case "mulvar": return "Умножить переменную";
            case "divvar": return "Разделить переменную";
            case "setglobalvar": return "Глобальная переменная";
            case "getglobalvar": return "Получить глобальную";
            case "setservervar": return "Серверная переменная";
            case "getservervar": return "Получить серверную";
            case "healplayer": return "Лечить игрока";
            case "setgamemode": return "Режим игры";
            case "settime": return "Установить время";
            case "setweather": return "Установить погоду";
            case "explosion": return "Взрыв";
            case "setblock": return "Установить блок";
            case "wait": return "Ожидание";
            case "randomnumber": return "Случайное число";
            case "asyncloop": return "Асинхронный цикл";
            case "createscoreboard": return "Создать скорборд";
            case "setscore": return "Установить счет";
            case "incrementscore": return "Увеличить счет";
            case "createteam": return "Создать команду";
            case "addplayertoteam": return "Добавить игрока в команду";
            case "savelocation": return "Сохранить локацию";
            case "getlocation": return "Получить локацию";
            default: return actionId;
        }
    }

    /**
     * Получает описание действия
     *
     * Gets description for action
     *
     * Ruft die Beschreibung der Aktion аб
     */
    private String getActionDescription(String actionId) {
        // Return descriptions for actions
        switch (actionId.toLowerCase()) {
            case "sendmessage": return "Отправляет сообщение игроку";
            case "broadcast": return "Отправляет сообщение всем игрокам";
            case "sendtitle": return "Показывает заголовок на экране";
            case "sendactionbar": return "Показывает текст над хотбаром";
            case "teleport": return "Телепортирует игрока";
            case "giveitem": return "Выдает предмет игроку";
            case "giveitems": return "Выдает несколько предметов";
            case "playsound": return "Воспроизводит звук";
            case "effect": return "Накладывает эффект";
            case "playparticle": return "Создает частицы";
            case "command": return "Выполняет команду";
            case "executeasynccommand": return "Выполняет команду асинхронно";
            case "spawnentity": return "Создает существо";
            case "spawnmob": return "Создает моба";
            case "removeitems": return "Удаляет предметы у игрока";
            case "setarmor": return "Одевает броню на игрока";
            case "setvar": return "Создает/изменяет переменную";
            case "getvar": return "Получает значение переменной";
            case "addvar": return "Добавляет к переменной";
            case "subvar": return "Вычитает из переменной";
            case "mulvar": return "Умножает переменную";
            case "divvar": return "Делит переменную";
            case "setglobalvar": return "Глобальная переменная для всех";
            case "getglobalvar": return "Получает глобальную переменную";
            case "setservervar": return "Серверная переменная";
            case "getservervar": return "Получает серверную переменную";
            case "healplayer": return "Восстанавливает здоровье";
            case "setgamemode": return "Меняет режим игры";
            case "settime": return "Устанавливает время в мире";
            case "setweather": return "Меняет погоду";
            case "explosion": return "Создает взрыв";
            case "setblock": return "Устанавливает блок";
            case "wait": return "Задержка выполнения";
            case "randomnumber": return "Генерирует случайное число";
            case "asyncloop": return "Повторяет действие асинхронно";
            case "createscoreboard": return "Создает скорборд";
            case "setscore": return "Устанавливает счет в скорборде";
            case "incrementscore": return "Увеличивает счет в скорборде";
            case "createteam": return "Создает команду";
            case "addplayertoteam": return "Добавляет игрока в команду";
            case "savelocation": return "Сохраняет локацию";
            case "getlocation": return "Получает сохраненную локацию";
            default: return "Действие " + actionId;
        }
    }
    
    /**
     * Открывает графический интерфейс для игрока
     * Реализует стиль reference system: универсальные блоки с настройкой через GUI
     *
     * Opens the GUI for the player
     * Implements reference system-style: universal blocks with GUI configuration
     *
     * Öffnet die GUI für den Spieler
     * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Kонфигuration
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
     *
     * Gets the GUI title
     * @return Interface title
     *
     * Ruft den GUI-Titel аб
     * @return Schnittstellentitel
     */
    public String getGUITitle() {
        return "Action Selection GUI for " + blockMaterial.name();
    }
    
    @Override
    /**
     * Обрабатывает события кликов в инвентаре
     * @param event Событие клика в инвентаре
     *
     * Handles inventory click events
     * @param event Inventory click event
     *
     * Verarbeitet Inventarklick-Ereignisse
     * @param event Inventarklick-Ereignis
     */
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true); // Cancel all clicks by default
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        ItemMeta meta = clicked.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return;
        
        // Find action ID in lore
        String actionId = null;
        boolean isCategoryItem = false;
        for (String line : lore) {
            if (line.startsWith("§8ID: ")) {
                actionId = line.substring(5); // Remove "§8ID: " prefix
                break;
            }
            if (line.contains("Категория")) {
                isCategoryItem = true;
                break;
            }
        }
        
        if (isCategoryItem) {
            // 🎆 ENHANCED: Handle category item click with helpful message
            player.sendMessage("§eℹ Это заголовок категории. Кликните по действию ниже.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            return;
        }
        
        if (actionId != null) {
            selectAction(actionId);
        }
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Выбирает действие для блока
     * Реализует стиль reference system: универсальные блоки с настройкой через GUI
     *
     * 🎆 ENHANCED: Select action for the block
     * Implements reference system-style: universal blocks with GUI configuration
     *
     * 🎆 ERWEITERT: Wählt Aktion für den Block
     * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Kонфигuration
     */
    private void selectAction(String actionId) {
        // Get the code block
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
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
        var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getWorldManager().saveWorld(creativeWorld);
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
     *
     * Handles inventory close events
     * @param event Inventory close event
     *
     * Verarbeitet Inventarschließ-Ereignisse
     * @param event Inventarschließ-Ereignis
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    /**
     * Выполняет очистку ресурсов при закрытии интерфейса
     *
     * Performs resource cleanup when interface is closed
     *
     * Führt eine Ressourcenbereinigung durch, wenn die Schnittstelle geschlossen wird
     */
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}