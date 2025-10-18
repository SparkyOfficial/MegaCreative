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
 * 🎆 УЛУЧШЕННЫЙ Графический интерфейс для выбора действий, событий и условий для блоков кода.
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 ENHANCED GUI for selecting actions, events, and conditions for code blocks.
 * 
 * Implements reference system-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 ERWEITERT GUI zur Auswahl von Aktionen, Ereignissen und Bedingungen für Codeblöcke.
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
 */
public class ActionSelectionGUI implements GUIManager.ManagedGUIInterface {
    
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
        
        CATEGORY_NAMES.put("EVENT", "🌟 События");
        CATEGORY_NAMES.put("ACTION", "⚡ Действия");
        CATEGORY_NAMES.put("CONDITION", "❓ Условия");
        CATEGORY_NAMES.put("CONTROL", "⚙️ Управление");
        CATEGORY_NAMES.put("FUNCTION", "📚 Функции");
        CATEGORY_NAMES.put("VARIABLE", "📊 Переменные");
        
        
        CATEGORY_MATERIALS.put("EVENT", Material.NETHER_STAR);
        CATEGORY_MATERIALS.put("ACTION", Material.REDSTONE);
        CATEGORY_MATERIALS.put("CONDITION", Material.COMPARATOR);
        CATEGORY_MATERIALS.put("CONTROL", Material.REPEATER);
        CATEGORY_MATERIALS.put("FUNCTION", Material.WRITABLE_BOOK);
        CATEGORY_MATERIALS.put("VARIABLE", Material.NAME_TAG);
    }
    
    /**
     * Инициализирует графический интерфейс выбора действий
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     * @param blockMaterial Материал блока для настройки
     */
    public ActionSelectionGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        
        
        // Condition plugin != null is always true
        // Removed redundant null check
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        
        this.inventory = Bukkit.createInventory(null, 54, "§8Выбор действия: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    /**
     * Получает отображаемое имя блока
     */
    private String getBlockDisplayName() {
        
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        if (config != null) {
            return config.getDisplayName();
        }
        
        
        for (BlockConfigService.BlockConfig blockConfig : blockConfigService.getAllBlockConfigs()) {
            if (blockConfig.getMaterial() == blockMaterial) {
                return blockConfig.getDisplayName();
            }
        }
        
        return blockMaterial.name();
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
        infoLore.add("§7Выберите категорию действий");
        infoLore.add("");
        infoLore.add("§aКликните по категории чтобы");
        infoLore.add("§aпросмотреть доступные действия");
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
    
    @Override
    /**
     * Получает заголовок графического интерфейса
     * @return Заголовок интерфейса
     */
    public String getGUITitle() {
        return "Action Selection GUI for " + blockMaterial.name();
    }
    
    @Override
    /**
     * Обрабатывает события кликов в инвентаре
     * @param event Событие клика в инвентаре
     */
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
        
        
        if (displayName.contains("Назад")) {
            
            setupInventory();
            player.openInventory(inventory);
            return;
        }
        
        
        List<String> lore = meta.getLore();
        if (lore != null) {
            
            String actionId = null;
            for (String line : lore) {
                if (line.startsWith("§8ID: ")) {
                    actionId = line.substring(5).trim(); 
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
        
        
        loadActionsForCategory(categoryInventory, category);
        
        
        player.openInventory(categoryInventory);
    }
    
    /**
     * Загружает действия для категории
     * @param inventory Инвентарь для заполнения
     * @param category Категория для загрузки
     */
    private void loadActionsForCategory(Inventory inventory, String category) {
        
        if (blockConfigService == null) {
            player.sendMessage("§cОшибка: Сервис конфигурации блоков недоступен!");
            return;
        }
        
        
        List<String> categoryActions = new ArrayList<>();
        
        
        for (BlockConfigService.BlockConfig config : blockConfigService.getAllBlockConfigs()) {
            
            if ("VARIABLE".equals(category) && blockMaterial == Material.IRON_BLOCK) {
                categoryActions.add(config.getId());
            }
            
            else if ("FUNCTION".equals(category) && 
                    (blockMaterial == Material.LAPIS_BLOCK || blockMaterial == Material.BOOKSHELF)) {
                categoryActions.add(config.getId());
            }
            
            else if (config.getType().equals(category)) {
                categoryActions.add(config.getId());
            }
            
            else if ("ACTION".equals(category) && !"EVENT".equals(config.getType())) {
                categoryActions.add(config.getId());
            }
        }
        
        
        if (categoryActions.isEmpty()) {
            for (BlockConfigService.BlockConfig config : blockConfigService.getAllBlockConfigs()) {
                
                categoryActions.add(config.getId());
            }
        }
        
        
        Collections.sort(categoryActions);
        
        
        int slot = 10;
        for (String actionId : categoryActions) {
            if (slot >= 44) break; 
            
            ItemStack actionItem = createActionItem(actionId);
            inventory.setItem(slot, actionItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2; 
        }
    }
    
    /**
     * Создает элемент действия
     * @param actionId ID действия
     * @return ItemStack элемент действия
     */
    private ItemStack createActionItem(String actionId) {
        
        Material material = getActionMaterial(actionId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        
        meta.setDisplayName("§a§l" + getActionDisplayName(actionId));
        
        
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
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(actionId);
        if (config != null) {
            String type = config.getType();
            return CATEGORY_NAMES.getOrDefault(type, type);
        }
        return "Другое";
    }
    
    /**
     * Получает материал для действия
     * @param actionId ID действия
     * @return Материал для действия
     */
    private Material getActionMaterial(String actionId) {
        
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
            case "onjoin":
            case "onleave":
            case "onchat":
                return Material.PLAYER_HEAD;
            case "onblockbreak":
            case "onblockplace":
                return Material.WOODEN_PICKAXE;
            case "onplayermove":
                return Material.LEATHER_BOOTS;
            case "onplayerdeath":
                return Material.SKELETON_SKULL;
            case "oncommand":
                return Material.COMMAND_BLOCK;
            case "ontick":
                return Material.CLOCK;
            case "ifvarequals":
            case "ifvargreater":
            case "ifvarless":
            case "comparevariable":
                return Material.COMPARATOR;
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
        
        // Switch branch is unreachable: the label contains an uppercase symbol while the selector is lowercase-only
        // Fixed by using the original actionId instead of lowercasing it
        switch (actionId) {
            
            case "onjoin": return "При входе";
            case "onleave": return "При выходе";
            case "onchat": return "При чате";
            case "onblockbreak": return "При разрушении блока";
            case "onblockplace": return "При установке блока";
            case "onplayermove": return "При движении игрока";
            case "onplayerdeath": return "При смерти игрока";
            case "oncommand": return "При команде";
            case "ontick": return "Каждый тик";
            
            
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

            case "createscoreboard": return "Создать скорборд";
            case "setscore": return "Установить счет";
            case "incrementscore": return "Увеличить счет";
            case "createteam": return "Создать команду";
            case "addplayertoteam": return "Добавить игрока в команду";
            case "savelocation": return "Сохранить локацию";
            case "getlocation": return "Получить локацию";
            
            
            case "ifvarequals": return "Если переменная равна";
            case "ifvargreater": return "Если переменная больше";
            case "ifvarless": return "Если переменная меньше";
            case "comparevariable": return "Сравнить переменные";
            case "isop": return "Если оператор";
            case "hasitem": return "Если есть предмет";
            case "haspermission": return "Если есть право";
            case "isinworld": return "Если в мире";
            case "worldtime": return "Если время мира";
            case "isnearblock": return "Если рядом блок";
            case "mobnear": return "Если рядом моб";
            case "playergamemode": return "Если режим игры";
            case "playerhealth": return "Если здоровье";
            case "isnight": return "Если ночь";
            case "isriding": return "Если едет";
            case "checkplayerinventory": return "Проверить инвентарь";
            case "checkplayerstats": return "Проверить статистику";
            case "checkserveronline": return "Проверить сервер";
            case "checkworldweather": return "Проверить погоду";
            case "worldguardregioncheck": return "Проверить регион";
            case "isblocktype": return "Если тип блока";
            case "isplayerholding": return "Если держит";
            case "hasarmor": return "Если есть броня";
            
            
            case "repeat": return "Повторить";
            case "repeattrigger": return "Повторить триггер";

            case "else": return "Иначе";
            case "openBracket": return "Открыть скобку";
            case "closeBracket": return "Закрыть скобку";
            
            
            case "callfunction": return "Вызвать функцию";
            case "savefunction": return "Сохранить функцию";
            
            default: return actionId;
        }
    }

    /**
     * Получает описание действия
     * @param actionId ID действия
     * @return Описание действия
     */
    private String getActionDescription(String actionId) {
        
        // Switch branch is unreachable: the label contains an uppercase symbol while the selector is lowercase-only
        // Fixed by using the original actionId instead of lowercasing it
        switch (actionId) {
            
            case "onjoin": return "Срабатывает когда игрок заходит на сервер";
            case "onleave": return "Срабатывает когда игрок выходит с сервера";
            case "onchat": return "Срабатывает когда игрок пишет в чат";
            case "onblockbreak": return "Срабатывает когда игрок ломает блок";
            case "onblockplace": return "Срабатывает когда игрок ставит блок";
            case "onplayermove": return "Срабатывает когда игрок двигается";
            case "onplayerdeath": return "Срабатывает когда игрок умирает";
            case "oncommand": return "Срабатывает когда игрок использует команду";
            case "ontick": return "Срабатывает каждый игровой тик";
            
            
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

            case "createscoreboard": return "Создает скорборд";
            case "setscore": return "Устанавливает счет в скорборде";
            case "incrementscore": return "Увеличивает счет в скорборде";
            case "createteam": return "Создает команду";
            case "addplayertoteam": return "Добавляет игрока в команду";
            case "savelocation": return "Сохраняет локацию";
            case "getlocation": return "Получает сохраненную локацию";
            
            
            case "ifvarequals": return "Проверяет равенство переменной";
            case "ifvargreater": return "Проверяет больше ли переменная";
            case "ifvarless": return "Проверяет меньше ли переменная";
            case "comparevariable": return "Сравнивает две переменные";
            case "isop": return "Проверяет является ли игрок оператором";
            case "hasitem": return "Проверяет есть ли предмет у игрока";
            case "haspermission": return "Проверяет есть ли право у игрока";
            case "isinworld": return "Проверяет находится ли игрок в мире";
            case "worldtime": return "Проверяет время в мире";
            case "isnearblock": return "Проверяет находится ли рядом блок";
            case "mobnear": return "Проверяет находится ли рядом моб";
            case "playergamemode": return "Проверяет режим игры игрока";
            case "playerhealth": return "Проверяет здоровье игрока";
            case "isnight": return "Проверяет ночь ли сейчас";
            case "isriding": return "Проверяет едет ли игрок";
            case "checkplayerinventory": return "Проверяет инвентарь игрока";
            case "checkplayerstats": return "Проверяет статистику игрока";
            case "checkserveronline": return "Проверяет статус сервера";
            case "checkworldweather": return "Проверяет погоду в мире";
            case "worldguardregioncheck": return "Проверяет находится ли игрок в регионе";
            case "isblocktype": return "Проверяет тип блока";
            case "isplayerholding": return "Проверяет что держит игрок";
            case "hasarmor": return "Проверяет есть ли броня у игрока";
            
            
            case "repeat": return "Повторяет действие";
            case "repeattrigger": return "Повторяет действие с триггером";

            case "else": return "Выполняет если предыдущее условие ложно";
            case "openBracket": return "Открывает скобку для группировки";
            case "closeBracket": return "Закрывает скобку для группировки";
            
            
            case "callfunction": return "Вызывает сохраненную функцию";
            case "savefunction": return "Сохраняет функцию для повторного использования";
            
            default: return "Действие " + actionId;
        }
    }
    
    /**
     * Выбирает действие для блока
     * @param actionId ID действия
     */
    private void selectAction(String actionId) {
        
        if (plugin.getServiceRegistry().getBlockPlacementHandler() == null) {
            player.sendMessage("§cОшибка: Не удалось получить обработчик блоков");
            return;
        }
        
        CodeBlock codeBlock = plugin.getServiceRegistry().getBlockPlacementHandler().getCodeBlock(blockLocation);
        if (codeBlock == null) {
            player.sendMessage("§cОшибка: Блок кода не найден");
            return;
        }
        
        
        codeBlock.setAction(actionId);
        
        

        
        var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
        }
        
        
        player.sendMessage("§a✓ Действие '" + getActionDisplayName(actionId) + "' установлено!");
        player.sendMessage("§eКликните снова по блоку для настройки параметров.");
        
        
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, 
            player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 1);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        
        
        player.closeInventory();
    }
    
    @Override
    /**
     * Обрабатывает события закрытия инвентаря
     * @param event Событие закрытия инвентаря
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        
        
    }
    
    @Override
    /**
     * Выполняет очистку ресурсов при закрытии интерфейса
     */
    public void onCleanup() {
        
        
    }
}